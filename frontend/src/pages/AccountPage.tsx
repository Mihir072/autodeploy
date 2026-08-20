import React from "react";
import { useAuth } from "@/lib/hooks/useAuth";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { GithubIcon } from "@/components/ui/icons";
import { User, ShieldCheck, Key, LogOut, ExternalLink, Server } from "lucide-react";

export function AccountPage() {
  const { user, logout, accessToken } = useAuth();

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white">Account & Profile</h1>
        <p className="text-xs text-muted-foreground mt-0.5">
          Manage your connected GitHub identity, security credentials, and platform plan
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
        {/* Left Column: User Card */}
        <Card className="border-border/80 bg-card/60 p-6 space-y-6 md:col-span-2">
          <div className="flex items-center gap-4">
            <img
              src={user?.avatarUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80"}
              alt={user?.username || "User"}
              className="h-16 w-16 rounded-full border border-white/20 object-cover"
            />
            <div className="space-y-1">
              <h3 className="font-bold text-lg text-white">{user?.username || "mihir072"}</h3>
              <p className="text-xs font-mono text-muted-foreground">{user?.email || "mihir@autodeploy.dev"}</p>
              <Badge variant="outline" className="text-[10px] font-mono border-emerald-500/30 text-emerald-400 bg-emerald-500/10">
                Connected via GitHub OAuth2
              </Badge>
            </div>
          </div>

          <div className="space-y-3 text-xs font-mono pt-4 border-t border-border/60 text-muted-foreground">
            <div className="flex justify-between py-1.5 border-b border-border/40">
              <span>User ID:</span>
              <span className="text-foreground">{user?.id || "usr_99812401"}</span>
            </div>
            <div className="flex justify-between py-1.5 border-b border-border/40">
              <span>GitHub ID:</span>
              <span className="text-foreground">{user?.githubId || "8472910"}</span>
            </div>
            <div className="flex justify-between py-1.5">
              <span>Rate Limit Allowance:</span>
              <span className="text-emerald-400">20 req/s (Burst 40)</span>
            </div>
          </div>
        </Card>

        {/* Right Column: Security info */}
        <div className="space-y-6">
          <Card className="border-border/80 bg-card/60 p-6 space-y-4">
            <CardHeader className="p-0">
              <CardTitle className="text-sm font-bold text-white flex items-center gap-2">
                <ShieldCheck className="h-4 w-4 text-emerald-400" />
                <span>Security & Tokens</span>
              </CardTitle>
            </CardHeader>
            <p className="text-xs text-muted-foreground leading-relaxed">
              Your session is authenticated via HS512 JWT tokens and refreshed automatically through the API Gateway.
            </p>
            <div className="pt-2">
              <Button
                variant="destructive"
                size="sm"
                onClick={() => logout()}
                className="w-full text-xs gap-2"
              >
                <LogOut className="h-4 w-4" />
                <span>Log Out of Session</span>
              </Button>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
