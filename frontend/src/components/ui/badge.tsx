import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
  {
    variants: {
      variant: {
        default:
          "border-transparent bg-primary text-primary-foreground shadow",
        secondary:
          "border-transparent bg-secondary text-secondary-foreground",
        destructive:
          "border-transparent bg-destructive text-destructive-foreground",
        outline: "text-foreground border-border",
        live: "border-status-live-border bg-status-live-bg text-status-live shadow-[0_0_12px_rgba(16,185,129,0.2)]",
        building: "border-status-building-border bg-status-building-bg text-status-building shadow-[0_0_12px_rgba(245,158,11,0.2)]",
        failed: "border-status-failed-border bg-status-failed-bg text-status-failed shadow-[0_0_12px_rgba(239,68,68,0.2)]",
        queued: "border-status-queued-border bg-status-queued-bg text-status-queued shadow-[0_0_12px_rgba(99,102,241,0.2)]",
        idle: "border-status-idle-border bg-status-idle-bg text-status-idle",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  );
}

export { Badge, badgeVariants };
