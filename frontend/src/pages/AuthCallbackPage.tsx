import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuthStore } from "@/store/useAuthStore";
import { authApi } from "@/lib/api/auth";
import { Loader2, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";

export function AuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const accessToken = searchParams.get("access_token");
    const refreshToken = searchParams.get("refresh_token");
    const errorParam = searchParams.get("error");

    if (errorParam) {
      setError(decodeURIComponent(errorParam));
      return;
    }

    if (!accessToken) {
      setError("No access token received from authentication provider.");
      return;
    }

    const processAuth = async () => {
      try {
        // Temporarily store token so apiClient interceptor can attach it
        useAuthStore.getState().setToken(accessToken);

        // Fetch the real authenticated user from auth-service
        const user = await authApi.getMe();
        setAuth(user, accessToken);

        // Store refresh token if present
        if (refreshToken) {
          localStorage.setItem("refresh_token", refreshToken);
        }

        // Navigate to dashboard upon successful authentication
        navigate("/dashboard", { replace: true });
      } catch (err: any) {
        console.error("Failed to complete authentication:", err);
        setError(
          err.response?.data?.message ||
            err.message ||
            "Failed to retrieve user profile from backend."
        );
      }
    };

    processAuth();
  }, [searchParams, navigate, setAuth]);

  if (error) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-xl p-8 text-center space-y-4">
          <div className="h-12 w-12 rounded-full bg-red-500/10 text-red-400 flex items-center justify-center mx-auto">
            <AlertCircle className="h-6 w-6" />
          </div>
          <h2 className="text-xl font-semibold text-white">Authentication Failed</h2>
          <p className="text-sm text-slate-400">{error}</p>
          <div className="pt-4">
            <Button
              className="w-full bg-primary hover:bg-primary/90 text-white"
              onClick={() => navigate("/login")}
            >
              Return to Login
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
      <div className="text-center space-y-4">
        <Loader2 className="h-10 w-10 animate-spin text-primary mx-auto" />
        <h2 className="text-lg font-medium text-white">Authenticating with GitHub...</h2>
        <p className="text-sm text-slate-400">Verifying session and loading your account...</p>
      </div>
    </div>
  );
}
