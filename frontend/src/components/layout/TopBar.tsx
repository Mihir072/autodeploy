import React from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { ChevronRight, ExternalLink, RefreshCw, RotateCcw, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DeploymentStatusBadge } from "@/components/features/DeploymentStatusBadge";
import { useProjects } from "@/lib/hooks/useProjects";

export function TopBar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { projectsQuery, triggerBuildMutation } = useProjects();

  const pathParts = location.pathname.split("/").filter(Boolean);
  const projects = projectsQuery.data || [];

  // Determine current project from URL if inside /projects/:id
  const isProjectRoute = pathParts[0] === "projects" && pathParts[1];
  const projectId = isProjectRoute ? pathParts[1] : null;
  const activeProject = projectId ? projects.find((p) => p.id === projectId || p.slug === projectId) : null;
  const isDeploymentRoute = isProjectRoute && pathParts[2] === "deployments" && pathParts[3];
  const deploymentId = isDeploymentRoute ? pathParts[3] : null;

  const handleQuickRedeploy = async () => {
    if (!activeProject) return;
    try {
      await triggerBuildMutation.mutateAsync({
        projectId: activeProject.id,
        branch: activeProject.branch,
      });
    } catch {
      // Handled gracefully
    }
  };

  return (
    <header className="h-16 border-b border-border/80 bg-background/80 backdrop-blur-md sticky top-0 z-20 px-6 flex items-center justify-between select-none">
      {/* Breadcrumb Navigation */}
      <nav className="flex items-center gap-2 text-xs font-mono text-muted-foreground overflow-hidden">
        <Link to="/dashboard" className="hover:text-white transition-colors">
          AutoDeploy
        </Link>

        {activeProject && (
          <>
            <ChevronRight className="h-3.5 w-3.5 text-zinc-600 flex-shrink-0" />
            <Link
              to={`/projects/${activeProject.id}`}
              className="text-foreground font-semibold hover:text-white transition-colors truncate max-w-[200px]"
            >
              {activeProject.name}
            </Link>
          </>
        )}

        {deploymentId && (
          <>
            <ChevronRight className="h-3.5 w-3.5 text-zinc-600 flex-shrink-0" />
            <span className="text-zinc-400 font-mono">
              deployment #{deploymentId.replace("dep_", "")}
            </span>
          </>
        )}

        {pathParts[2] === "settings" && (
          <>
            <ChevronRight className="h-3.5 w-3.5 text-zinc-600 flex-shrink-0" />
            <span className="text-zinc-400">settings</span>
          </>
        )}

        {pathParts[2] === "domains" && (
          <>
            <ChevronRight className="h-3.5 w-3.5 text-zinc-600 flex-shrink-0" />
            <span className="text-zinc-400">domains</span>
          </>
        )}

        {pathParts[0] === "new" && (
          <>
            <ChevronRight className="h-3.5 w-3.5 text-zinc-600 flex-shrink-0" />
            <span className="text-zinc-300">import repository</span>
          </>
        )}
      </nav>

      {/* Action Buttons & Status Badge */}
      <div className="flex items-center gap-3 flex-shrink-0">
        {activeProject && activeProject.latestDeployment && (
          <DeploymentStatusBadge status={activeProject.latestDeployment.status} size="sm" />
        )}

        {activeProject && (
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleQuickRedeploy}
              disabled={triggerBuildMutation.isPending}
              className="h-8 text-xs gap-1.5"
            >
              <RefreshCw className={`h-3.5 w-3.5 ${triggerBuildMutation.isPending ? "animate-spin" : ""}`} />
              <span>Redeploy</span>
            </Button>

            {activeProject.latestDeployment?.url && (
              <a
                href={activeProject.latestDeployment.url}
                target="_blank"
                rel="noreferrer"
                className="inline-flex items-center justify-center h-8 px-3 rounded-md text-xs font-semibold bg-white text-black hover:bg-neutral-200 transition-colors gap-1.5 shadow-sm"
              >
                <span>Visit Site</span>
                <ExternalLink className="h-3.5 w-3.5" />
              </a>
            )}
          </div>
        )}
      </div>
    </header>
  );
}
