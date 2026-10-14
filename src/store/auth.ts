import { create } from 'zustand'

type AuthState = {
  accessToken: string | null
  refreshToken: string | null
  email: string | null
  setSession: (accessToken: string, refreshToken: string, email: string) => void
  setTokens: (accessToken: string, refreshToken: string) => void
  clearSession: () => void
}

export const useAuth = create<AuthState>((set) => ({
  accessToken: null,
  refreshToken: null,
  email: null,
  setSession: (accessToken, refreshToken, email) => set({ accessToken, refreshToken, email }),
  setTokens: (accessToken, refreshToken) => set({ accessToken, refreshToken }),
  clearSession: () => set({ accessToken: null, refreshToken: null, email: null }),
}))