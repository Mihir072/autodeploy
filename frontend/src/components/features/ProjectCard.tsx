import React from "react";
import { Link } from "react-router-dom";
import { Project } from "@/types";
import { DeploymentStatusBadge } from "./DeploymentStatusBadge";
import { formatRelativeTime, truncateCommitSha } from "@/lib/utils";
import { FolderGit2, GitBranch, ExternalLink, Globe, Clock, ArrowRight } from "lucide-react";

interface ProjectCardProps {
  project: Project;
}

export function ProjectCard({ project }: ProjectCardProps) {
  const latest = project.latestDeployment;

  return (
    <div className="group relative rounded-xl border border-border/80 bg-card/70 backdrop-blur-sm p-6 hover:border-white/20 transition-all duration-200 hover:shadow-[0_8px_30px_rgba(0,0,0,0.4)] flex flex-col justify-between space-y-6">
      {/* Top Header info */}
      <div className="space-y-4">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-lg bg-zinc-900 border border-white/10 flex items-center justify-center text-zinc-300 group-hover:text-white group-hover:border-white/20 transition-colors">
              <FolderGit2 className="h-5 w-5" />
            </div>
            <div>
              <Link
                to={`/projects/${project.id}`}
                className="font-bold text-base text-foreground group-hover:text-white transition-colors flex items-center gap-1.5"
              >
                <span>{project.name}</span>
                <ArrowRight className="h-3.5 w-3.5 opacity-0 -translate-x-1 group-hover:opacity-100 group-hover:translate-x-0 transition-all text-muted-foreground" />
              </Link>
              <div className="flex items-center gap-2 text-xs text-muted-foreground font-mono mt-0.5">
                <a
                  href={project.repositoryUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="hover:text-white transition-colors truncate max-w-[200px]"
                >
                  {project.repositoryName}
                </a>
              </div>
            </div>
          </div>

          {latest && <DeploymentStatusBadge status={latest.status} size="sm" />}
        </div>

        {/* Live URL Link */}
        {project.subdomain && (
          <div className="flex items-center gap-2 text-xs font-mono">
            <Globe className="h-3.5 w-3.5 text-zinc-500" />
            <a
              href={latest?.url || `https://${project.subdomain}`}
              target="_blank"
              rel="noreferrer"
              className="text-zinc-300 hover:text-white hover:underline transition-colors flex items-center gap-1 truncate"
            >
              <span>{project.customDomain || project.subdomain}</span>
              <ExternalLink className="h-3 w-3 text-zinc-500" />
            </a>
          </div>
        )}
      </div>

      {/* Footer Metadata */}
      <div className="pt-4 border-t border-border/60 flex items-center justify-between text-xs text-muted-foreground font-mono">
        <div className="flex items-center gap-2">
          <GitBranch className="h-3.5 w-3.5 text-zinc-500" />
          <span>{project.branch}</span>
          {latest && (
            <>
              <span className="text-zinc-700">•</span>
              <span className="text-zinc-400">{truncateCommitSha(latest.commitSha)}</span>
            </>
          )}
        </div>

        <div className="flex items-center gap-1.5 text-zinc-500">
          <Clock className="h-3 w-3" />
          <span>{formatRelativeTime(latest?.createdAt || project.updatedAt)}</span>
        </div>
      </div>
    </div>
  );
}
