import { useSearchParams } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { GithubIcon } from "@/components/ui/icons";
import { useAuth } from "@/lib/hooks/useAuth";
import { ShieldCheck, AlertCircle } from "lucide-react";

export function LoginPage() {
  const [searchParams] = useSearchParams();
  const { loginWithGithub, isLoading } = useAuth();
  const error = searchParams.get("error");

  return (
    <div className="min-h-screen bg-[#09090b] text-foreground flex items-center justify-center p-6 selection:bg-white/20 selection:text-white">
      <div className="w-full max-w-md space-y-6">
        <div className="text-center space-y-2">
          <div className="h-10 w-10 rounded-xl bg-white flex items-center justify-center text-black font-black mx-auto shadow-[0_0_30px_rgba(255,255,255,0.3)]">
            ▲
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Log in to AutoDeploy</h1>
          <p className="text-xs text-muted-foreground">
            Connect with your GitHub account to manage projects, repositories & deployments
          </p>
        </div>

        {error && (
          <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-lg flex items-center gap-2 text-xs text-red-400">
            <AlertCircle className="h-4 w-4 shrink-0" />
            <span>{decodeURIComponent(error)}</span>
          </div>
        )}

        <Card className="border-border/80 bg-card/80 backdrop-blur-xl p-2 shadow-2xl">
          <CardContent className="space-y-4 pt-6">
            <Button
              className="w-full h-11 text-xs font-semibold gap-2.5 bg-white text-black hover:bg-zinc-200 transition-all shadow-[0_0_20px_rgba(255,255,255,0.2)]"
              onClick={loginWithGithub}
              disabled={isLoading}
            >
              <GithubIcon className="h-4 w-4" />
              <span>Continue with GitHub</span>
            </Button>
          </CardContent>
        </Card>

        <div className="flex items-center justify-center gap-2 text-xs text-zinc-500 font-mono">
          <ShieldCheck className="h-3.5 w-3.5 text-emerald-500" />
          <span>OAuth2 Token Encrypted with AES-256-GCM in PostgreSQL</span>
        </div>
      </div>
    </div>
  );
}
