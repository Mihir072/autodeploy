import React, { useState } from "react";
import { Link } from "react-router-dom";
import { useProjects } from "@/lib/hooks/useProjects";
import { ProjectCard } from "@/components/features/ProjectCard";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Plus, Search, FolderGit2, RefreshCw, Zap, Server, Activity } from "lucide-react";

export function DashboardPage() {
  const { projectsQuery } = useProjects();
  const [search, setSearch] = useState("");

  const projects = projectsQuery.data || [];
  const filteredProjects = projects.filter(
    (p) =>
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.repositoryName.toLowerCase().includes(search.toLowerCase()) ||
      p.slug.toLowerCase().includes(search.toLowerCase())
  );

  const liveProjectsCount = projects.filter((p) => p.latestDeployment?.status === "LIVE").length;
  const buildingProjectsCount = projects.filter(
    (p) => p.latestDeployment?.status === "BUILDING" || p.latestDeployment?.status === "DEPLOYING"
  ).length;

  return (
    <div className="space-y-8">
      {/* Top Banner & Stats */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-extrabold tracking-tight text-white">
            Projects Overview
          </h1>
          <p className="text-xs text-muted-foreground mt-1">
            Deployments managed across multi-cloud infrastructure & edge routers
          </p>
        </div>

        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="sm"
            onClick={() => projectsQuery.refetch()}
            disabled={projectsQuery.isFetching}
            className="h-9 text-xs gap-1.5 border-white/10"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${projectsQuery.isFetching ? "animate-spin" : ""}`} />
            <span>Refresh</span>
          </Button>

          <Link to="/new">
            <Button size="sm" className="h-9 text-xs gap-2 font-semibold shadow-[0_0_20px_rgba(255,255,255,0.2)]">
              <Plus className="h-4 w-4" />
              <span>Import Repository</span>
            </Button>
          </Link>
        </div>
      </div>

      {/* Metrics Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="p-4 rounded-xl border border-border/80 bg-card/60 flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-xs text-muted-foreground font-mono uppercase">Total Projects</span>
            <div className="text-2xl font-bold text-white font-mono">{projects.length}</div>
          </div>
          <div className="h-10 w-10 rounded-lg bg-zinc-900 border border-white/10 flex items-center justify-center text-zinc-300">
            <FolderGit2 className="h-5 w-5" />
          </div>
        </div>

        <div className="p-4 rounded-xl border border-border/80 bg-card/60 flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-xs text-emerald-400 font-mono uppercase">Live Deployments</span>
            <div className="text-2xl font-bold text-emerald-400 font-mono">{liveProjectsCount}</div>
          </div>
          <div className="h-10 w-10 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Zap className="h-5 w-5" />
          </div>
        </div>

        <div className="p-4 rounded-xl border border-border/80 bg-card/60 flex items-center justify-between">
          <div className="space-y-1">
            <span className="text-xs text-amber-400 font-mono uppercase">Active Builds</span>
            <div className="text-2xl font-bold text-amber-400 font-mono">{buildingProjectsCount}</div>
          </div>
          <div className="h-10 w-10 rounded-lg bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
            <Activity className="h-5 w-5" />
          </div>
        </div>
      </div>

      {/* Search Input Bar */}
      <div className="relative max-w-md">
        <Search className="absolute left-3 top-2.5 h-4 w-4 text-muted-foreground" />
        <Input
          type="text"
          placeholder="Filter projects by name, repo, or slug..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-9 bg-card/60 border-border/80 text-xs h-9"
        />
      </div>

      {/* Projects Grid / Empty State */}
      {projectsQuery.isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-48 rounded-xl border border-border/60 bg-card/40 animate-pulse" />
          ))}
        </div>
      ) : filteredProjects.length === 0 ? (
        <div className="p-12 rounded-2xl border border-dashed border-border/80 bg-card/20 text-center space-y-4 max-w-md mx-auto">
          <div className="h-12 w-12 rounded-xl bg-zinc-900 border border-white/10 flex items-center justify-center text-zinc-400 mx-auto">
            <FolderGit2 className="h-6 w-6" />
          </div>
          <div>
            <h3 className="font-bold text-base text-white">No projects found</h3>
            <p className="text-xs text-muted-foreground mt-1">
              Import a repository from your connected GitHub account to trigger your first build.
            </p>
          </div>
          <Link to="/new">
            <Button size="sm" className="text-xs gap-2 font-semibold">
              <Plus className="h-4 w-4" />
              <span>Import a repository</span>
            </Button>
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProjects.map((project) => (
            <ProjectCard key={project.id} project={project} />
          ))}
        </div>
      )}
    </div>
  );
}
