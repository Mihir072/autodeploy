import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { GithubIcon } from "@/components/ui/icons";
import { DeploymentStatusBadge } from "@/components/features/DeploymentStatusBadge";
import { useAuth } from "@/lib/hooks/useAuth";
import {
  Shield,
  Zap,
  Globe,
  ArrowRight,
} from "lucide-react";

export function LandingPage() {
  const navigate = useNavigate();
  const { isAuthenticated, loginWithGithub } = useAuth();

  const handleAction = () => {
    if (isAuthenticated) {
      navigate("/dashboard");
    } else {
      loginWithGithub();
    }
  };

  return (
    <div className="min-h-screen bg-[#09090b] text-foreground selection:bg-white/20 selection:text-white flex flex-col">
      {/* Top Navbar */}
      <header className="border-b border-white/[0.08] bg-[#09090b]/80 backdrop-blur-xl sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="h-8 w-8 rounded-lg bg-white flex items-center justify-center text-black font-black shadow-[0_0_25px_rgba(255,255,255,0.3)]">
              ▲
            </div>
            <span className="font-bold text-lg tracking-tight text-white">AutoDeploy</span>
          </div>

          <div className="flex items-center gap-4">
            {isAuthenticated ? (
              <Button
                size="sm"
                variant="default"
                onClick={() => navigate("/dashboard")}
                className="font-medium gap-2 text-xs h-9 px-4 bg-white text-black hover:bg-zinc-200"
              >
                Go to Dashboard
              </Button>
            ) : (
              <Button
                size="sm"
                variant="default"
                onClick={loginWithGithub}
                className="font-medium gap-2 text-xs h-9 px-4 bg-white text-black hover:bg-zinc-200 shadow-[0_0_15px_rgba(255,255,255,0.2)]"
              >
                <GithubIcon className="h-4 w-4" />
                <span>Continue with GitHub</span>
              </Button>
            )}
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1 max-w-7xl mx-auto px-6 py-20 md:py-28 space-y-24">
        <section className="text-center space-y-8 max-w-4xl mx-auto">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-white/10 bg-white/[0.03] text-xs font-mono text-zinc-300">
            <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
            <span>Multi-Service Microservices Platform • Spring Boot 3 + Java 21</span>
          </div>

          <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight text-white leading-[1.1]">
            Develop. Push to GitHub. <br />
            <span className="bg-gradient-to-r from-white via-zinc-300 to-zinc-600 bg-clip-text text-transparent">
              AutoDeploy takes care of the rest.
            </span>
          </h1>

          <p className="text-muted-foreground text-lg md:text-xl max-w-2xl mx-auto leading-relaxed">
            The next-generation CI/CD auto-deployment platform. Built with rootless Kaniko container builds,
            automatic Route53 DNS, Let's Encrypt SSL, and real-time live log streaming.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
            <Button
              size="lg"
              onClick={handleAction}
              className="h-12 px-8 font-semibold text-sm gap-2 bg-white text-black hover:bg-zinc-200 shadow-[0_0_30px_rgba(255,255,255,0.25)] w-full sm:w-auto"
            >
              <GithubIcon className="h-5 w-5" />
              <span>{isAuthenticated ? "Open Dashboard" : "Deploy Your First Repository"}</span>
              <ArrowRight className="h-4 w-4 ml-1" />
            </Button>
          </div>
        </section>

        {/* Live Product Showcase Preview */}
        <section className="rounded-2xl border border-white/10 bg-[#0d0d12] shadow-2xl p-6 md:p-8 space-y-6">
          <div className="flex items-center justify-between border-b border-white/10 pb-4">
            <div className="flex items-center gap-3">
              <div className="h-3 w-3 rounded-full bg-emerald-500 animate-ping" />
              <span className="text-sm font-semibold text-white font-mono">Live Build & Deploy Pipeline</span>
            </div>
            <DeploymentStatusBadge status="LIVE" size="sm" />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Project info card */}
            <div className="p-5 rounded-xl border border-white/5 bg-[#121217] space-y-4">
              <div className="text-xs font-mono text-muted-foreground uppercase">Architecture</div>
              <div>
                <h3 className="font-bold text-base text-white">Full-Stack Cloud Infrastructure</h3>
                <p className="text-xs text-muted-foreground font-mono mt-0.5">PostgreSQL • Redis • RabbitMQ • Spring Cloud</p>
              </div>
              <div className="space-y-2 text-xs font-mono pt-2 border-t border-white/5">
                <div className="flex justify-between text-zinc-400">
                  <span>Auth:</span>
                  <span className="text-white">GitHub OAuth2 + JWT</span>
                </div>
                <div className="flex justify-between text-zinc-400">
                  <span>Builds:</span>
                  <span className="text-emerald-400">Kaniko Rootless</span>
                </div>
                <div className="flex justify-between text-zinc-400">
                  <span>Proxy:</span>
                  <span className="text-blue-400">Traefik Edge Routing</span>
                </div>
              </div>
            </div>

            {/* Architecture description */}
            <div className="lg:col-span-2 p-5 rounded-xl border border-white/5 bg-[#08080b] font-mono text-xs text-zinc-400 space-y-1.5 overflow-hidden">
              <div className="text-[11px] text-zinc-600 flex items-center justify-between pb-2 border-b border-white/5">
                <span>DEPLOYMENT ENGINE LOGS</span>
                <span className="text-emerald-400">STATUS READY</span>
              </div>
              <div className="text-zinc-500">[SYSTEM] API Gateway configured on port 9090 with rate limiting and JWT validation</div>
              <div className="text-zinc-400">[SERVICES] auth-service, project-service, build-service, deployment-service, domain-service registered</div>
              <div className="text-zinc-300">[DATABASE] Dedicated PostgreSQL databases configured for each service</div>
              <div className="text-blue-400">[SECURITY] AES-256-GCM encrypted GitHub tokens and environment variables</div>
              <div className="text-emerald-400 font-semibold">[READY] Connect your GitHub repository to trigger production builds</div>
            </div>
          </div>
        </section>

        {/* Feature Grid */}
        <section className="space-y-12">
          <div className="text-center space-y-3">
            <h2 className="text-3xl md:text-4xl font-bold tracking-tight text-white">
              Engineered for Speed, Isolation, and Scale
            </h2>
            <p className="text-muted-foreground text-sm max-w-xl mx-auto">
              Every stage of the deployment lifecycle is separated across dedicated Spring Boot microservices.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card className="border-white/10 bg-[#121217]/70 p-6 space-y-4">
              <div className="h-10 w-10 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
                <Zap className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-lg text-white">Instant GitHub Webhooks</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Automatic trigger on git push. Authenticated via HMAC SHA-256 signatures and queued onto RabbitMQ.
              </p>
            </Card>

            <Card className="border-white/10 bg-[#121217]/70 p-6 space-y-4">
              <div className="h-10 w-10 rounded-lg bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-blue-400">
                <Shield className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-lg text-white">Rootless Kaniko Builds</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Secure container image generation without host Docker daemon exposure. Pushed directly to private ECR.
              </p>
            </Card>

            <Card className="border-white/10 bg-[#121217]/70 p-6 space-y-4">
              <div className="h-10 w-10 rounded-lg bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400">
                <Globe className="h-5 w-5" />
              </div>
              <h3 className="font-bold text-lg text-white">Automated DNS & SSL</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Dynamic AWS Route53 A/CNAME provisioning with automatic Let's Encrypt TLS certificates.
              </p>
            </Card>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="border-t border-white/[0.08] bg-[#09090b] py-8 px-6 text-center text-xs text-zinc-500 font-mono">
        AutoDeploy Platform • Java 21 · Spring Cloud · React 18 · TypeScript · Tailwind CSS
      </footer>
    </div>
  );
}
