import React from "react";
import { DeploymentStatus } from "@/types";
import { cn } from "@/lib/utils";
import { CheckCircle2, Loader2, AlertCircle, Clock, Ban } from "lucide-react";

interface DeploymentStatusBadgeProps {
  status: DeploymentStatus;
  className?: string;
  size?: "sm" | "default" | "lg";
  showIcon?: boolean;
}

export const DeploymentStatusBadge: React.FC<DeploymentStatusBadgeProps> = ({
  status,
  className,
  size = "default",
  showIcon = true,
}) => {
  const getStatusConfig = (s: DeploymentStatus) => {
    switch (s) {
      case "LIVE":
        return {
          label: "Ready",
          icon: CheckCircle2,
          classes: "border-emerald-500/30 bg-emerald-500/10 text-emerald-400 shadow-[0_0_12px_rgba(16,185,129,0.15)]",
          dotColor: "bg-emerald-500",
        };
      case "BUILDING":
        return {
          label: "Building",
          icon: Loader2,
          iconSpin: true,
          classes: "border-amber-500/30 bg-amber-500/10 text-amber-400 shadow-[0_0_12px_rgba(245,158,11,0.15)]",
          dotColor: "bg-amber-500 animate-ping",
        };
      case "DEPLOYING":
        return {
          label: "Deploying",
          icon: Loader2,
          iconSpin: true,
          classes: "border-blue-500/30 bg-blue-500/10 text-blue-400 shadow-[0_0_12px_rgba(59,130,246,0.15)]",
          dotColor: "bg-blue-500 animate-ping",
        };
      case "QUEUED":
        return {
          label: "Queued",
          icon: Clock,
          classes: "border-indigo-500/30 bg-indigo-500/10 text-indigo-400",
          dotColor: "bg-indigo-500",
        };
      case "FAILED":
        return {
          label: "Error",
          icon: AlertCircle,
          classes: "border-red-500/30 bg-red-500/10 text-red-400 shadow-[0_0_12px_rgba(239,68,68,0.15)]",
          dotColor: "bg-red-500",
        };
      case "CANCELLED":
        return {
          label: "Cancelled",
          icon: Ban,
          classes: "border-zinc-700 bg-zinc-800/40 text-zinc-400",
          dotColor: "bg-zinc-500",
        };
      case "IDLE":
      default:
        return {
          label: "Idle",
          icon: Clock,
          classes: "border-zinc-700 bg-zinc-800/40 text-zinc-400",
          dotColor: "bg-zinc-500",
        };
    }
  };

  const config = getStatusConfig(status);
  const Icon = config.icon;

  const sizeClasses = {
    sm: "px-2 py-0.5 text-xs gap-1.5",
    default: "px-2.5 py-1 text-xs gap-1.5 font-medium",
    lg: "px-3 py-1.5 text-sm gap-2 font-medium",
  }[size];

  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border transition-all select-none",
        config.classes,
        sizeClasses,
        className
      )}
    >
      {showIcon && (
        <span className="relative flex h-2 w-2 items-center justify-center">
          <span className={cn("h-1.5 w-1.5 rounded-full", config.dotColor)} />
        </span>
      )}
      <span>{config.label}</span>
    </span>
  );
};
