import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Combines class names with Tailwind merge support.
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Formats a date or timestamp to relative time (e.g., '2 mins ago', 'yesterday').
 */
export function formatRelativeTime(dateInput?: string | number | Date | null): string {
  if (!dateInput) return "Never";
  const date = new Date(dateInput);
  if (isNaN(date.getTime())) return "Invalid date";

  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (diffInSeconds < 30) return "Just now";
  if (diffInSeconds < 60) return `${diffInSeconds}s ago`;

  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) return `${diffInMinutes}m ago`;

  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `${diffInHours}h ago`;

  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 30) return `${diffInDays}d ago`;

  return date.toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
}

/**
 * Truncates commit hash (e.g. 'a8f3b49e...' -> 'a8f3b49')
 */
export function truncateCommitSha(sha?: string | null): string {
  if (!sha) return "HEAD";
  return sha.substring(0, 7);
}

/**
 * Formats execution duration (e.g., 45 -> '45s', 125 -> '2m 5s')
 */
export function formatDuration(durationSeconds?: number | null): string {
  if (durationSeconds == null || durationSeconds < 0) return "--";
  if (durationSeconds < 60) return `${durationSeconds}s`;
  const minutes = Math.floor(durationSeconds / 60);
  const seconds = durationSeconds % 60;
  return `${minutes}m ${seconds}s`;
}
