import 'dotenv/config'
import { createHmac, timingSafeEqual } from 'node:crypto'
import { createServer } from 'node:http'
import axios from 'axios'
import cors from 'cors'
import express from 'express'
import { rateLimit } from 'express-rate-limit'
import helmet from 'helmet'
import jwt from 'jsonwebtoken'
import { Server } from 'socket.io'

const port = Number(process.env.PORT ?? 4000)
const app = express()
const httpServer = createServer(app)
const io = new Server(httpServer, {
  cors: { origin: process.env.CLIENT_ORIGIN ?? 'http://localhost:5173', methods: ['GET', 'POST'] },
})

app.use(helmet())
app.use(cors({ origin: process.env.CLIENT_ORIGIN ?? 'http://localhost:5173' }))
app.use(rateLimit({ windowMs: 15 * 60 * 1000, limit: 180, standardHeaders: 'draft-7', legacyHeaders: false }))
app.use(express.json({ limit: '32kb', verify: (request, _response, body) => { request.rawBody = body } }))

app.get('/health', (_request, response) => response.json({ status: 'ok', service: 'aube-realtime' }))

app.post('/webhooks/orders', (request, response) => {
  const secret = process.env.ORDER_WEBHOOK_SECRET
  const signature = request.header('x-aube-signature') ?? ''
  if (!secret || !request.rawBody) return response.status(503).json({ error: 'Webhook verification is not configured' })
  const expected = createHmac('sha256', secret).update(request.rawBody).digest()
  let received
  try { received = Buffer.from(signature, 'hex') } catch { return response.status(401).json({ error: 'Invalid signature' }) }
  if (received.length !== expected.length || !timingSafeEqual(received, expected)) return response.status(401).json({ error: 'Invalid signature' })

  const { orderId, status } = request.body ?? {}
  if (!orderId || !status) return response.status(400).json({ error: 'orderId and status are required' })
  io.to(`order:${orderId}`).emit('order:status', { orderId, status, updatedAt: new Date().toISOString() })
  return response.status(202).json({ delivered: true })
})

io.use((socket, next) => {
  const token = socket.handshake.auth?.token
  try {
    if (!token || !process.env.JWT_SECRET_BASE64) throw new Error('Missing token')
    socket.data.claims = jwt.verify(token, process.env.JWT_SECRET, { algorithms: ['HS256'] })
    next()
  } catch {
    next(new Error('Unauthorized'))
  }
})

io.on('connection', (socket) => {
  socket.on('order:watch', async (orderId, acknowledge = () => {}) => {
    if (typeof orderId !== 'string' || !/^[A-Za-z0-9-]{1,80}$/.test(orderId)) return acknowledge({ error: 'Invalid order id' })
    try {
      const response = await axios.get(`${process.env.API_BASE_URL ?? 'http://localhost:8080'}/api/v1/orders/${encodeURIComponent(orderId)}`, {
        headers: { authorization: `Bearer ${socket.handshake.auth.token}` }, timeout: 3000,
      })
      if (String(response.data.userId) !== String(socket.data.claims.subId) && !socket.data.claims.roles?.includes('ROLE_ADMIN')) return acknowledge({ error: 'Forbidden' })
      socket.join(`order:${orderId}`)
      acknowledge({ joined: true, status: response.data.status })
    } catch {
      acknowledge({ error: 'Order could not be verified' })
    }
  })
})

httpServer.listen(port, () => console.log(`Aube realtime service listening on ${port}`))