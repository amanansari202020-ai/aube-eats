import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { MenuItem } from '../data/menu'

type CartEntry = { item: MenuItem; quantity: number }
type CartState = {
  items: CartEntry[]
  add: (item: MenuItem) => void
  update: (id: string, quantity: number) => void
  clear: () => void
}

export const useCart = create<CartState>()(persist((set) => ({
  items: [],
  add: (item) => set((state) => {
    const match = state.items.find((entry) => entry.item.id === item.id)
    return { items: match ? state.items.map((entry) => entry.item.id === item.id ? { ...entry, quantity: entry.quantity + 1 } : entry) : [...state.items, { item, quantity: 1 }] }
  }),
  update: (id, quantity) => set((state) => ({ items: quantity <= 0 ? state.items.filter((entry) => entry.item.id !== id) : state.items.map((entry) => entry.item.id === id ? { ...entry, quantity } : entry) })),
  clear: () => set({ items: [] }),
}), { name: 'aube-eats-cart' }))