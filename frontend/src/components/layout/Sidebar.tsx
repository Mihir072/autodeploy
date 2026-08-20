import React, { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import {
  LayoutDashboard,
  PlusCircle,
  FolderGit2,
  Settings,
  Globe,
  Terminal,
  ChevronDown,
  Layers,
  LogOut,
  ExternalLink,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { useAuth } from "@/lib/hooks/useAuth";
import { useProjects } from "@/lib/hooks/useProjects";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

export function Sidebar() {
  const location = useLocation();
  const { user, logout } = useAuth();
  const { projectsQuery } = useProjects();
  const [collapsed, setCollapsed] = useState(false);
  const [projectMenuOpen, setProjectMenuOpen] = useState(false);

  const projects = projectsQuery.data || [];
  const currentProjectId = location.pathname.split("/projects/")[1]?.split("/")[0];
  const activeProject = projects.find((p) => p.id === currentProjectId || p.slug === currentProjectId);

  const navItems = [
    { label: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
    { label: "New Project", href: "/new", icon: PlusCircle },
  ];

  const projectNavItems = activeProject
    ? [
        { label: "Overview", href: `/projects/${activeProject.id}`, icon: Layers },
        { label: "Domains", href: `/projects/${activeProject.id}/domains`, icon: Globe },
        { label: "Settings", href: `/projects/${activeProject.id}/settings`, icon: Settings },
      ]
    : [];

  return (
    <aside
      className={cn(
        "relative flex flex-col border-r border-border/80 bg-background/95 backdrop-blur-xl transition-all duration-300 z-30 select-none",
        collapsed ? "w-16" : "w-64"
      )}
    >
      {/* Brand Header */}
      <div className="h-16 flex items-center justify-between px-4 border-b border-border/80">
        <Link to="/dashboard" className="flex items-center gap-3 overflow-hidden">
          <div className="h-8 w-8 rounded-lg bg-white flex items-center justify-center text-black font-black flex-shrink-0 shadow-[0_0_20px_rgba(255,255,255,0.25)]">
            ▲
          </div>
          {!collapsed && (
            <div className="flex flex-col">
              <span className="font-bold text-sm tracking-tight text-white flex items-center gap-1.5">
                AutoDeploy
                <Badge variant="outline" className="text-[9px] px-1.5 py-0 h-4 border-white/10 text-muted-foreground font-mono">
                  v1.0
                </Badge>
              </span>
              <span className="text-[11px] text-muted-foreground">Platform Console</span>
            </div>
          )}
        </Link>
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="p-1 rounded-md text-muted-foreground hover:text-white hover:bg-white/5 transition-colors"
          title={collapsed ? "Expand sidebar" : "Collapse sidebar"}
        >
          {collapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
        </button>
      </div>

      {/* Project Switcher Selector */}
      {!collapsed && (
        <div className="p-3 border-b border-border/60">
          <button
            onClick={() => setProjectMenuOpen(!projectMenuOpen)}
            className="w-full flex items-center justify-between p-2 rounded-md bg-card/70 border border-white/5 hover:border-white/15 text-left text-xs transition-colors group"
          >
            <div className="flex items-center gap-2 truncate">
              <FolderGit2 className="h-4 w-4 text-muted-foreground group-hover:text-white" />
              <span className="truncate font-medium text-foreground">
                {activeProject ? activeProject.name : "Select Project..."}
              </span>
            </div>
            <ChevronDown className={cn("h-3.5 w-3.5 text-muted-foreground transition-transform", projectMenuOpen && "rotate-180")} />
          </button>

          {projectMenuOpen && (
            <div className="mt-2 p-1 rounded-md bg-card border border-border shadow-xl space-y-0.5 animate-in fade-in-50">
              {projects.map((p) => (
                <Link
                  key={p.id}
                  to={`/projects/${p.id}`}
                  onClick={() => setProjectMenuOpen(false)}
                  className={cn(
                    "flex items-center justify-between px-2.5 py-1.5 rounded text-xs hover:bg-white/5 transition-colors",
                    (activeProject?.id === p.id) && "bg-white/10 font-medium text-white"
                  )}
                >
                  <span className="truncate">{p.name}</span>
                  <span className="text-[10px] font-mono text-muted-foreground">{p.branch}</span>
                </Link>
              ))}
              <div className="pt-1 mt-1 border-t border-border/60">
                <Link
                  to="/new"
                  onClick={() => setProjectMenuOpen(false)}
                  className="flex items-center gap-1.5 px-2.5 py-1.5 rounded text-xs text-primary hover:bg-white/5"
                >
                  <PlusCircle className="h-3.5 w-3.5" />
                  <span>Import New Project</span>
                </Link>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Main Navigation */}
      <div className="flex-1 overflow-y-auto p-3 space-y-6">
        {/* Core Global Links */}
        <div className="space-y-1">
          {!collapsed && <p className="px-2 text-[10px] font-mono uppercase tracking-wider text-muted-foreground mb-1">Platform</p>}
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.href;
            return (
              <Link
                key={item.href}
                to={item.href}
                className={cn(
                  "flex items-center gap-3 px-3 py-2 rounded-md text-xs font-medium transition-colors",
                  isActive
                    ? "bg-white/10 text-white font-semibold shadow-sm"
                    : "text-muted-foreground hover:bg-white/5 hover:text-white"
                )}
                title={collapsed ? item.label : undefined}
              >
                <Icon className="h-4 w-4 flex-shrink-0" />
                {!collapsed && <span>{item.label}</span>}
              </Link>
            );
          })}
        </div>

        {/* Project Specific Links */}
        {activeProject && (
          <div className="space-y-1 pt-2 border-t border-border/40">
            {!collapsed && (
              <p className="px-2 text-[10px] font-mono uppercase tracking-wider text-muted-foreground mb-1 truncate">
                {activeProject.name}
              </p>
            )}
            {projectNavItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.href;
              return (
                <Link
                  key={item.href}
                  to={item.href}
                  className={cn(
                    "flex items-center gap-3 px-3 py-2 rounded-md text-xs font-medium transition-colors",
                    isActive
                      ? "bg-white/10 text-white font-semibold"
                      : "text-muted-foreground hover:bg-white/5 hover:text-white"
                  )}
                  title={collapsed ? item.label : undefined}
                >
                  <Icon className="h-4 w-4 flex-shrink-0" />
                  {!collapsed && <span>{item.label}</span>}
                </Link>
              );
            })}
          </div>
        )}
      </div>

      {/* User Footer Profile */}
      <div className="p-3 border-t border-border/80 bg-background/50">
        <div className="flex items-center justify-between">
          <Link
            to="/account"
            className="flex items-center gap-2.5 overflow-hidden p-1 rounded-md hover:bg-white/5 transition-colors flex-1"
          >
            <img
              src={user?.avatarUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80"}
              alt={user?.username || "User"}
              className="h-7 w-7 rounded-full border border-white/20 object-cover flex-shrink-0"
            />
            {!collapsed && (
              <div className="flex flex-col truncate">
                <span className="text-xs font-medium text-white truncate">{user?.username || "mihir072"}</span>
                <span className="text-[10px] font-mono text-muted-foreground truncate">{user?.email || "Pro Plan"}</span>
              </div>
            )}
          </Link>
          {!collapsed && (
            <button
              onClick={() => logout()}
              title="Sign Out"
              className="p-1.5 text-muted-foreground hover:text-red-400 hover:bg-white/5 rounded transition-colors"
            >
              <LogOut className="h-4 w-4" />
            </button>
          )}
        </div>
      </div>
    </aside>
  );
}
