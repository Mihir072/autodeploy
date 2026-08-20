import React from "react";
import { DeploymentStatus } from "@/types";
import { CheckCircle2, Circle, Clock, Loader2, XCircle, AlertTriangle } from "lucide-react";
import { cn, formatDuration } from "@/lib/utils";

interface DeploymentTimelineProps {
  status: DeploymentStatus;
  durationSeconds?: number;
  errorMessage?: string;
}

export function DeploymentTimeline({
  status,
  durationSeconds,
  errorMessage,
}: DeploymentTimelineProps) {
  const steps = [
    { key: "QUEUED", label: "Queued", description: "Build task dispatched to RabbitMQ" },
    { key: "BUILDING", label: "Building", description: "Kaniko compiling container image & pushing to ECR" },
    { key: "DEPLOYING", label: "Deploying", description: "EC2 runner executing docker run with Traefik labels" },
    { key: "LIVE", label: "Live", description: "Healthcheck passed, routing active on global edge" },
  ];

  const getStepState = (stepIndex: number) => {
    const statusOrder: Record<DeploymentStatus, number> = {
      QUEUED: 0,
      BUILDING: 1,
      DEPLOYING: 2,
      LIVE: 3,
      FAILED: 2, // Fails during build or deploy
      CANCELLED: 1,
      IDLE: -1,
    };

    const currentOrder = statusOrder[status] ?? -1;

    if (status === "FAILED" && stepIndex === currentOrder) {
      return "failed";
    }
    if (stepIndex < currentOrder || (status === "LIVE" && stepIndex === 3)) {
      return "completed";
    }
    if (stepIndex === currentOrder && status !== "FAILED") {
      return "current";
    }
    return "upcoming";
  };

  return (
    <div className="space-y-4 select-none">
      <div className="flex items-center justify-between">
        <h4 className="text-xs font-mono uppercase tracking-wider text-muted-foreground">
          Deployment Pipeline Timeline
        </h4>
        {durationSeconds && (
          <span className="text-xs font-mono text-zinc-400">
            Total Duration: {formatDuration(durationSeconds)}
          </span>
        )}
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
        {steps.map((step, idx) => {
          const state = getStepState(idx);
          return (
            <div
              key={step.key}
              className={cn(
                "p-4 rounded-xl border transition-all flex flex-col justify-between space-y-2",
                state === "completed" && "border-emerald-500/30 bg-emerald-500/5 text-foreground",
                state === "current" && "border-amber-500/40 bg-amber-500/10 shadow-[0_0_15px_rgba(245,158,11,0.15)]",
                state === "failed" && "border-red-500/40 bg-red-500/10 shadow-[0_0_15px_rgba(239,68,68,0.15)]",
                state === "upcoming" && "border-border/60 bg-card/40 text-muted-foreground opacity-60"
              )}
            >
              <div className="flex items-center justify-between">
                <span className="text-xs font-mono text-muted-foreground">Step 0{idx + 1}</span>
                {state === "completed" && <CheckCircle2 className="h-4 w-4 text-emerald-400" />}
                {state === "current" && <Loader2 className="h-4 w-4 text-amber-400 animate-spin" />}
                {state === "failed" && <XCircle className="h-4 w-4 text-red-400" />}
                {state === "upcoming" && <Circle className="h-3.5 w-3.5 text-zinc-600" />}
              </div>

              <div>
                <div className="font-semibold text-sm text-white">{step.label}</div>
                <p className="text-[11px] text-muted-foreground leading-tight mt-0.5">
                  {step.description}
                </p>
              </div>
            </div>
          );
        })}
      </div>

      {status === "FAILED" && errorMessage && (
        <div className="p-3.5 rounded-lg border border-red-500/30 bg-red-500/10 text-red-300 text-xs font-mono flex items-start gap-2.5">
          <AlertTriangle className="h-4 w-4 text-red-400 flex-shrink-0 mt-0.5" />
          <div>
            <div className="font-semibold text-red-200">Pipeline Error:</div>
            <div>{errorMessage}</div>
          </div>
        </div>
      )}
    </div>
  );
}
