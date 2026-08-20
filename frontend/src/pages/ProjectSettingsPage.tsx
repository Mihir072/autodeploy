import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useProjects } from "@/lib/hooks/useProjects";
import { EnvVarEditor } from "@/components/features/EnvVarEditor";
import { DomainStatusCard } from "@/components/features/DomainStatusCard";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { EnvironmentVariable } from "@/types";
import {
  Settings,
  KeyRound,
  Globe,
  Sliders,
  Trash2,
  Save,
  Plus,
  AlertTriangle,
} from "lucide-react";

export function ProjectSettingsPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const navigate = useNavigate();
  const {
    useProject,
    useDomains,
    updateProjectMutation,
    deleteProjectMutation,
    addDomainMutation,
    verifyDomainMutation,
    deleteDomainMutation,
  } = useProjects();

  const { data: project, isLoading } = useProject(projectId);
  const { data: domains = [] } = useDomains(projectId);

  const [projectName, setProjectName] = useState(project?.name || "");
  const [branch, setBranch] = useState(project?.branch || "main");
  const [buildCommand, setBuildCommand] = useState(project?.buildCommand || "npm run build");
  const [outputDirectory, setOutputDirectory] = useState(project?.outputDirectory || ".next");
  const [envVars, setEnvVars] = useState<EnvironmentVariable[]>(project?.environmentVariables || []);
  const [newDomain, setNewDomain] = useState("");
  const [savedSuccess, setSavedSuccess] = useState(false);

  // Sync state if project loads after render
  React.useEffect(() => {
    if (project) {
      setProjectName(project.name);
      setBranch(project.branch);
      setBuildCommand(project.buildCommand || "npm run build");
      setOutputDirectory(project.outputDirectory || ".next");
      setEnvVars(project.environmentVariables || []);
    }
  }, [project]);

  if (isLoading || !project) {
    return <div className="h-64 bg-card/40 rounded-xl animate-pulse" />;
  }

  const handleSaveGeneral = async (e: React.FormEvent) => {
    e.preventDefault();
    await updateProjectMutation.mutateAsync({
      id: project.id,
      data: {
        name: projectName,
        branch,
        buildCommand,
        outputDirectory,
        environmentVariables: envVars,
      },
    });
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 2500);
  };

  const handleAddDomain = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newDomain.trim()) return;
    await addDomainMutation.mutateAsync({
      projectId: project.id,
      domainName: newDomain.trim(),
    });
    setNewDomain("");
  };

  const handleDeleteProject = async () => {
    if (window.confirm(`Are you sure you want to delete "${project.name}"? This action cannot be undone.`)) {
      await deleteProjectMutation.mutateAsync(project.id);
      navigate("/dashboard");
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-white">Project Settings</h1>
        <p className="text-xs text-muted-foreground mt-0.5">
          Configure production branches, environment variables, custom domains, and build triggers
        </p>
      </div>

      <Tabs defaultValue="general" className="space-y-6">
        <TabsList className="bg-card/70 border border-border/80 p-1">
          <TabsTrigger value="general" className="text-xs gap-2">
            <Settings className="h-3.5 w-3.5" />
            <span>General</span>
          </TabsTrigger>
          <TabsTrigger value="env" className="text-xs gap-2">
            <KeyRound className="h-3.5 w-3.5" />
            <span>Environment Variables</span>
          </TabsTrigger>
          <TabsTrigger value="domains" className="text-xs gap-2">
            <Globe className="h-3.5 w-3.5" />
            <span>Domains</span>
          </TabsTrigger>
          <TabsTrigger value="build" className="text-xs gap-2">
            <Sliders className="h-3.5 w-3.5" />
            <span>Build & Deploy</span>
          </TabsTrigger>
        </TabsList>

        {/* General Settings */}
        <TabsContent value="general" className="space-y-6">
          <Card className="border-border/80 bg-card/60 p-6 space-y-6">
            <CardHeader className="p-0">
              <CardTitle className="text-base">Project Name</CardTitle>
              <CardDescription className="text-xs">
                Used to identify your project across the dashboard and deployment URLs
              </CardDescription>
            </CardHeader>
            <div className="max-w-md space-y-3">
              <Input
                type="text"
                value={projectName}
                onChange={(e) => setProjectName(e.target.value)}
                className="h-9 font-mono text-xs"
              />
              <Button
                size="sm"
                onClick={handleSaveGeneral}
                disabled={updateProjectMutation.isPending}
                className="text-xs gap-1.5"
              >
                <Save className="h-3.5 w-3.5" />
                <span>{savedSuccess ? "Saved!" : "Save Changes"}</span>
              </Button>
            </div>
          </Card>

          {/* Danger Zone */}
          <Card className="border-red-500/30 bg-red-500/5 p-6 space-y-4">
            <CardHeader className="p-0">
              <CardTitle className="text-base text-red-400 flex items-center gap-2">
                <AlertTriangle className="h-4 w-4" />
                <span>Danger Zone</span>
              </CardTitle>
              <CardDescription className="text-xs text-red-300/80">
                Permanently delete this project, all associated build artifacts, and DNS records.
              </CardDescription>
            </CardHeader>
            <div>
              <Button
                variant="destructive"
                size="sm"
                onClick={handleDeleteProject}
                className="text-xs gap-1.5"
              >
                <Trash2 className="h-3.5 w-3.5" />
                <span>Delete Project</span>
              </Button>
            </div>
          </Card>
        </TabsContent>

        {/* Environment Variables Tab */}
        <TabsContent value="env" className="space-y-6">
          <Card className="border-border/80 bg-card/60 p-6 space-y-6">
            <CardHeader className="p-0">
              <CardTitle className="text-base">Environment Variables</CardTitle>
              <CardDescription className="text-xs">
                Injected into container builds and execution runners at deploy time
              </CardDescription>
            </CardHeader>
            <EnvVarEditor variables={envVars} onChange={setEnvVars} />
            <div className="pt-2">
              <Button
                size="sm"
                onClick={handleSaveGeneral}
                disabled={updateProjectMutation.isPending}
                className="text-xs gap-1.5"
              >
                <Save className="h-3.5 w-3.5" />
                <span>{savedSuccess ? "Saved!" : "Save Variables"}</span>
              </Button>
            </div>
          </Card>
        </TabsContent>

        {/* Domains Tab */}
        <TabsContent value="domains" className="space-y-6">
          <Card className="border-border/80 bg-card/60 p-6 space-y-6">
            <CardHeader className="p-0">
              <CardTitle className="text-base">Add Custom Domain</CardTitle>
              <CardDescription className="text-xs">
                Assign your custom domain (e.g. app.mycompany.com) with automatic Let's Encrypt SSL
              </CardDescription>
            </CardHeader>
            <form onSubmit={handleAddDomain} className="flex gap-3 max-w-lg">
              <Input
                type="text"
                placeholder="example.com or sub.example.com"
                value={newDomain}
                onChange={(e) => setNewDomain(e.target.value)}
                className="h-9 font-mono text-xs"
                required
              />
              <Button type="submit" size="sm" className="text-xs gap-1.5 flex-shrink-0">
                <Plus className="h-4 w-4" />
                <span>Add Domain</span>
              </Button>
            </form>
          </Card>

          <div className="space-y-4">
            {domains.map((dom) => (
              <DomainStatusCard
                key={dom.id}
                domain={dom}
                onVerify={(id) => verifyDomainMutation.mutate(id)}
                onDelete={(id) => deleteDomainMutation.mutate(id)}
                isVerifying={verifyDomainMutation.isPending}
              />
            ))}
          </div>
        </TabsContent>

        {/* Build & Deploy Tab */}
        <TabsContent value="build" className="space-y-6">
          <Card className="border-border/80 bg-card/60 p-6 space-y-6">
            <CardHeader className="p-0">
              <CardTitle className="text-base">Build & Output Settings</CardTitle>
              <CardDescription className="text-xs">
                Configure build commands, output paths, and git branch triggers
              </CardDescription>
            </CardHeader>

            <div className="space-y-4 text-xs max-w-lg">
              <div className="space-y-1.5">
                <label className="font-mono text-[11px] text-zinc-400">PRODUCTION BRANCH</label>
                <Input
                  type="text"
                  value={branch}
                  onChange={(e) => setBranch(e.target.value)}
                  className="h-8 font-mono"
                />
              </div>

              <div className="space-y-1.5">
                <label className="font-mono text-[11px] text-zinc-400">BUILD COMMAND</label>
                <Input
                  type="text"
                  value={buildCommand}
                  onChange={(e) => setBuildCommand(e.target.value)}
                  className="h-8 font-mono"
                />
              </div>

              <div className="space-y-1.5">
                <label className="font-mono text-[11px] text-zinc-400">OUTPUT DIRECTORY</label>
                <Input
                  type="text"
                  value={outputDirectory}
                  onChange={(e) => setOutputDirectory(e.target.value)}
                  className="h-8 font-mono"
                />
              </div>

              <div className="pt-2">
                <Button
                  size="sm"
                  onClick={handleSaveGeneral}
                  disabled={updateProjectMutation.isPending}
                  className="text-xs gap-1.5"
                >
                  <Save className="h-3.5 w-3.5" />
                  <span>{savedSuccess ? "Saved!" : "Save Build Settings"}</span>
                </Button>
              </div>
            </div>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
