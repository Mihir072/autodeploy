import apiClient from "./client";
import { User, ApiResponse } from "@/types";

export const authApi = {
  /**
   * Fetch currently authenticated user profile
   */
  getMe: async (): Promise<User> => {
    const response = await apiClient.get<ApiResponse<User>>("/api/auth/me");
    return response.data.data;
  },

  /**
   * Refresh JWT access token
   */
  refresh: async (): Promise<{ accessToken: string }> => {
    const response = await apiClient.post<ApiResponse<{ accessToken: string }>>("/api/auth/refresh");
    return response.data.data;
  },

  /**
   * Logout user and invalidate session
   */
  logout: async (): Promise<void> => {
    await apiClient.post("/api/auth/logout");
  },

  /**
   * Get GitHub OAuth login redirection URL
   */
  getGithubAuthUrl: (): string => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:9090";
    return `${baseUrl}/oauth2/authorization/github`;
  },
};
