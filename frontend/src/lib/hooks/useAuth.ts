import { useEffect } from "react";
import { useAuthStore } from "@/store/useAuthStore";
import { authApi } from "@/lib/api/auth";

export function useAuth() {
  const { user, accessToken, isAuthenticated, isLoading, logout: storeLogout, setLoading, setUser } =
    useAuthStore();

  useEffect(() => {
    // Attempt to verify session from backend on load if access token is present
    const checkSession = async () => {
      if (accessToken) {
        try {
          const profile = await authApi.getMe();
          setUser(profile);
        } catch (error) {
          // Token is invalid/expired — clear the stale session
          storeLogout();
        }
      }
      setLoading(false);
    };

    checkSession();
  }, [accessToken, setLoading, setUser, storeLogout]);

  const loginWithGithub = () => {
    const authUrl = authApi.getGithubAuthUrl();
    window.location.href = authUrl;
  };

  const logout = async () => {
    try {
      await authApi.logout();
    } catch {
      // Ignore network errors during logout
    } finally {
      localStorage.removeItem("refresh_token");
      storeLogout();
      window.location.href = "/login";
    }
  };

  return {
    user,
    accessToken,
    isAuthenticated,
    isLoading,
    loginWithGithub,
    logout,
  };
}
