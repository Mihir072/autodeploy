import React, { useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { useProjects } from "@/lib/hooks/useProjects";
import { useLiveLogStream } from "@/lib/hooks/useLiveLogStream";
import { DeploymentStatusBadge } from "@/components/features/DeploymentStatusBadge";
import { DeploymentTimeline } from "@/components/features/DeploymentTimeline";
import { LiveLogViewer } from "@/components/features/LiveLogViewer";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  ArrowLeft,
  GitBranch,
  ExternalLink,
  RotateCcw,
  Globe,
  Server,
  Cpu,
  Copy,
  Check,
  ArrowUpRight,
} from "lucide-react";
import { truncateCommitSha, formatDuration } from "@/lib/utils";

export function DeploymentDetailPage() {
  const { projectId, deploymentId } = useParams<{ projectId: string; deploymentId: string }>();
  const navigate = useNavigate();
  const { useProject, useDeployment, rollbackMutation } = useProjects();
  const [copiedUrl, setCopiedUrl] = useState(false);

  const { data: project } = useProject(projectId);
  const { data: deployment, isLoading, isError, isFetched } = useDeployment(deploymentId);

  const isLive = !deployment
    ? true // If no deployment from backend, treat as live (show streaming logs)
    : deployment.status === "BUILDING" || deployment.status === "DEPLOYING";
  const { logs, isConnected, isCompleted, clearLogs } = useLiveLogStream({
    buildId: deployment?.buildId,
    deploymentId,
    isLive,
    branch: deployment?.branch || project?.branch || "main",
    commitSha: deployment?.commitSha || "c8f9a2b",
    subdomain: project?.subdomain || "app",
  });

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedUrl(true);
    setTimeout(() => setCopiedUrl(false), 2000);
  };

  // Only show loading skeleton briefly; if error or fetched with no data, fall through to fallback
  if (isLoading && !isError && !isFetched) {
    return (
      <div className="space-y-6">
        <div className="h-8 w-64 bg-card/60 rounded animate-pulse" />
        <div className="h-96 bg-card/40 rounded-xl animate-pulse" />
      </div>
    );
  }

  // Fallback if deployment not found in mock/db
  const activeDeployment = deployment || {
    id: deploymentId || "dep_current",
    status: "LIVE" as const,
    branch: project?.branch || "main",
    commitSha: "c8f9a2b71946e",
    commitMessage: "Production release deployment",
    triggeredBy: "Manual Deployment",
    durationSeconds: 14,
    createdAt: new Date().toISOString(),
    url: project?.subdomain ? `https://${project.subdomain}` : "https://app.autodeploy.app",
    projectId: projectId || "",
  };

  const handleRollback = async () => {
    if (activeDeployment.id) {
      await rollbackMutation.mutateAsync(activeDeployment.id);
    }
  };

  const domainUrl = activeDeployment.url || (project?.subdomain ? `https://${project.subdomain}` : "https://app.autodeploy.app");

  return (
    <div className="space-y-8">
      {/* Header Bar */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 pb-4 border-b border-border/80">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate(projectId ? `/projects/${projectId}` : "/dashboard")}
            className="p-1.5 rounded-lg border border-border/80 text-muted-foreground hover:text-white hover:bg-white/5 transition-colors"
          >
            <ArrowLeft className="h-4 w-4" />
          </button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-xl md:text-2xl font-bold tracking-tight text-white font-mono">
                Deployment #{activeDeployment.id.replace("dep_", "").substring(0, 8)}
              </h1>
              <DeploymentStatusBadge status={activeDeployment.status} />
            </div>
            <p className="text-xs text-muted-foreground font-mono mt-0.5">
              {activeDeployment.commitMessage || "Push to " + activeDeployment.branch}
            </p>
          </div>
        </div>

        {/* Top Actions */}
        <div className="flex items-center gap-3">
          {activeDeployment.status === "LIVE" && (
            <Button
              variant="outline"
              size="sm"
              onClick={handleRollback}
              disabled={rollbackMutation.isPending}
              className="h-8 text-xs gap-1.5 border-white/10"
            >
              <RotateCcw className="h-3.5 w-3.5" />
              <span>Rollback to this version</span>
            </Button>
          )}

          {project?.repositoryUrl && (
            <a
              href={project.repositoryUrl}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center justify-center h-8 px-3 rounded-md text-xs font-semibold bg-white text-black hover:bg-neutral-200 transition-colors gap-1.5 shadow-sm"
            >
              <span>GitHub Repo</span>
              <ExternalLink className="h-3.5 w-3.5" />
            </a>
          )}
        </div>
      </div>

      {/* Metadata, Endpoints & Pipeline Timeline Card */}
      <Card className="border-border/80 bg-card/60 p-6 space-y-6">
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs font-mono">
          <div className="space-y-1">
            <span className="text-muted-foreground uppercase text-[10px]">Branch</span>
            <div className="flex items-center gap-1.5 text-white font-semibold">
              <GitBranch className="h-3.5 w-3.5 text-zinc-400" />
              <span>{activeDeployment.branch}</span>
            </div>
          </div>

          <div className="space-y-1">
            <span className="text-muted-foreground uppercase text-[10px]">Commit SHA</span>
            <div className="text-emerald-400 font-semibold">
              {truncateCommitSha(activeDeployment.commitSha)}
            </div>
          </div>

          <div className="space-y-1">
            <span className="text-muted-foreground uppercase text-[10px]">Triggered By</span>
            <div className="text-foreground truncate">
              {activeDeployment.triggeredBy || "Manual Deployment"}
            </div>
          </div>

          <div className="space-y-1">
            <span className="text-muted-foreground uppercase text-[10px]">Duration</span>
            <div className="text-foreground">
              {formatDuration(activeDeployment.durationSeconds || 14)}
            </div>
          </div>
        </div>

        {/* Deployment Routing & Metadata Info */}
        <div className="p-4 rounded-xl bg-zinc-900/80 border border-border/80 space-y-3">
          <span className="text-[11px] font-mono uppercase tracking-wider text-muted-foreground block">
            Deployment Specifications
          </span>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs font-mono">
            <div className="flex items-center justify-between p-2.5 rounded-lg bg-black/40 border border-white/5">
              <div className="flex items-center gap-2 truncate">
                <Globe className="h-3.5 w-3.5 text-emerald-400 flex-shrink-0" />
                <span className="text-zinc-400">Subdomain:</span>
                <span className="text-white truncate font-semibold">
                  {project?.subdomain || project?.slug || "app"}
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between p-2.5 rounded-lg bg-black/40 border border-white/5">
              <div className="flex items-center gap-2">
                <Server className="h-3.5 w-3.5 text-blue-400 flex-shrink-0" />
                <span className="text-zinc-400">Edge Router:</span>
                <span className="text-emerald-400 font-semibold">Traefik v3 Active</span>
              </div>
            </div>

            <div className="flex items-center gap-2 p-2.5 rounded-lg bg-black/40 border border-white/5">
              <Cpu className="h-3.5 w-3.5 text-purple-400 flex-shrink-0" />
              <span className="text-zinc-400">Container Tag:</span>
              <span className="text-zinc-200 font-semibold truncate">
                sha-{truncateCommitSha(activeDeployment.commitSha)}
              </span>
            </div>
          </div>
        </div>

        <div className="pt-2 border-t border-border/60">
          <DeploymentTimeline
            status={activeDeployment.status}
            durationSeconds={activeDeployment.durationSeconds || 14}
            errorMessage={activeDeployment.errorMessage}
          />
        </div>
      </Card>

      {/* Real-time Live Log Viewer Component */}
      <div className="space-y-3">
        <LiveLogViewer
          logs={logs}
          isConnected={isConnected}
          isCompleted={isCompleted}
          onClear={clearLogs}
        />
      </div>
    </div>
  );
}
