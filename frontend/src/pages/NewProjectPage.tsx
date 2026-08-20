import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useProjects } from "@/lib/hooks/useProjects";
import { buildsApi } from "@/lib/api/builds";
import { deploymentsApi } from "@/lib/api/deployments";
import { GitHubRepository, EnvironmentVariable } from "@/types";
import { RepoPicker } from "@/components/features/RepoPicker";
import { EnvVarEditor } from "@/components/features/EnvVarEditor";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ArrowLeft, Rocket, FolderGit2, Settings, KeyRound, CheckCircle2, ChevronRight } from "lucide-react";

export function NewProjectPage() {
  const navigate = useNavigate();
  const { reposQuery, createProjectMutation } = useProjects();

  const [selectedRepo, setSelectedRepo] = useState<GitHubRepository | null>(null);
  const [projectName, setProjectName] = useState("");
  const [branch, setBranch] = useState("main");
  const [buildCommand, setBuildCommand] = useState("npm run build");
  const [outputDirectory, setOutputDirectory] = useState(".next");
  const [dockerfilePath, setDockerfilePath] = useState("");
  const [envVars, setEnvVars] = useState<EnvironmentVariable[]>([
    { key: "NODE_ENV", value: "production", isSecret: false },
  ]);

  const handleSelectRepo = (repo: GitHubRepository) => {
    setSelectedRepo(repo);
    setProjectName(repo.name);
    setBranch(repo.defaultBranch);
  };

  const handleDeploy = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRepo || !projectName) return;

    try {
      const created = await createProjectMutation.mutateAsync({
        name: projectName,
        repositoryName: selectedRepo.fullName,
        repositoryUrl: `https://github.com/${selectedRepo.fullName}`,
        branch,
        buildCommand: buildCommand || undefined,
        outputDirectory: outputDirectory || undefined,
        dockerfilePath: dockerfilePath || undefined,
        environmentVariables: envVars.filter((v) => v.key.trim() !== ""),
      });

      try {
        // Automatically trigger initial build and deployment for the project
        const build = await buildsApi.triggerBuild({
          projectId: created.id,
          branch,
        });

        const deployment = await deploymentsApi.createDeployment({
          projectId: created.id,
          buildId: build.id,
          branch,
          commitSha: build.commitSha,
        });

        navigate(`/projects/${created.id}/deployments/${deployment.id}`);
      } catch (deployErr) {
        console.warn("Could not trigger immediate build:", deployErr);
        navigate(`/projects/${created.id}`);
      }
    } catch (err) {
      console.error("Failed to create project:", err);
      navigate("/dashboard");
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      {/* Header */}
      <div className="flex items-center gap-3">
        <button
          onClick={() => navigate(-1)}
          className="p-1.5 rounded-lg border border-border/80 text-muted-foreground hover:text-white hover:bg-white/5 transition-colors"
        >
          <ArrowLeft className="h-4 w-4" />
        </button>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Import Git Repository</h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Select a repository from your GitHub account and configure build settings
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8 items-start">
        {/* Left Column: Repository Picker */}
        <div className="md:col-span-2 space-y-6">
          <Card className="border-border/80 bg-card/60 p-6">
            <CardHeader className="p-0 pb-4">
              <CardTitle className="text-base flex items-center gap-2">
                <FolderGit2 className="h-4 w-4 text-muted-foreground" />
                <span>1. Select GitHub Repository</span>
              </CardTitle>
              <CardDescription className="text-xs">
                Choose the repository containing your source code
              </CardDescription>
            </CardHeader>
            <CardContent className="p-0">
              <RepoPicker
                repositories={reposQuery.data || []}
                selectedRepo={selectedRepo}
                onSelectRepo={handleSelectRepo}
                isLoading={reposQuery.isLoading}
              />
            </CardContent>
          </Card>

          {/* Build Configuration Card */}
          {selectedRepo && (
            <form onSubmit={handleDeploy} className="space-y-6 animate-fade-in">
              <Card className="border-border/80 bg-card/60 p-6 space-y-6">
                <CardHeader className="p-0">
                  <CardTitle className="text-base flex items-center gap-2">
                    <Settings className="h-4 w-4 text-muted-foreground" />
                    <span>2. Build & Output Configuration</span>
                  </CardTitle>
                  <CardDescription className="text-xs">
                    Auto-detected framework settings. Override if your repository requires custom commands.
                  </CardDescription>
                </CardHeader>

                <div className="space-y-4 text-xs">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                      <label className="font-mono text-[11px] text-zinc-400">PROJECT NAME</label>
                      <Input
                        type="text"
                        value={projectName}
                        onChange={(e) => setProjectName(e.target.value)}
                        className="h-8 font-mono"
                        required
                      />
                    </div>

                    <div className="space-y-1.5">
                      <label className="font-mono text-[11px] text-zinc-400">PRODUCTION BRANCH</label>
                      <Input
                        type="text"
                        value={branch}
                        onChange={(e) => setBranch(e.target.value)}
                        className="h-8 font-mono"
                        required
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                      <label className="font-mono text-[11px] text-zinc-400">BUILD COMMAND</label>
                      <Input
                        type="text"
                        placeholder="npm run build"
                        value={buildCommand}
                        onChange={(e) => setBuildCommand(e.target.value)}
                        className="h-8 font-mono"
                      />
                    </div>

                    <div className="space-y-1.5">
                      <label className="font-mono text-[11px] text-zinc-400">OUTPUT DIRECTORY</label>
                      <Input
                        type="text"
                        placeholder=".next / dist / build"
                        value={outputDirectory}
                        onChange={(e) => setOutputDirectory(e.target.value)}
                        className="h-8 font-mono"
                      />
                    </div>
                  </div>

                  <div className="space-y-1.5">
                    <label className="font-mono text-[11px] text-zinc-400">DOCKERFILE PATH (OPTIONAL)</label>
                    <Input
                      type="text"
                      placeholder="Dockerfile (leave blank to auto-detect)"
                      value={dockerfilePath}
                      onChange={(e) => setDockerfilePath(e.target.value)}
                      className="h-8 font-mono"
                    />
                  </div>
                </div>
              </Card>

              {/* Environment Variables */}
              <Card className="border-border/80 bg-card/60 p-6">
                <EnvVarEditor variables={envVars} onChange={setEnvVars} />
              </Card>

              {/* Deploy Trigger Button */}
              <div className="flex justify-end pt-2">
                <Button
                  type="submit"
                  disabled={createProjectMutation.isPending}
                  className="h-11 px-8 text-xs font-semibold gap-2 shadow-[0_0_25px_rgba(255,255,255,0.25)]"
                >
                  <Rocket className="h-4 w-4" />
                  <span>{createProjectMutation.isPending ? "Triggering Build..." : "Deploy Repository"}</span>
                </Button>
              </div>
            </form>
          )}
        </div>

        {/* Right Column: Deployment Summary Card */}
        <div className="space-y-6 sticky top-24">
          <Card className="border-border/80 bg-card/60 p-6 space-y-4">
            <CardHeader className="p-0">
              <CardTitle className="text-sm font-bold text-white">Import Summary</CardTitle>
            </CardHeader>
            <div className="space-y-3 text-xs font-mono text-muted-foreground">
              <div className="flex justify-between py-1.5 border-b border-border/40">
                <span>Repository:</span>
                <span className="text-foreground truncate max-w-[140px]">
                  {selectedRepo ? selectedRepo.name : "--"}
                </span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-border/40">
                <span>Branch:</span>
                <span className="text-foreground">{branch}</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-border/40">
                <span>Builder:</span>
                <span className="text-emerald-400">Kaniko (Rootless)</span>
              </div>
              <div className="flex justify-between py-1.5 border-b border-border/40">
                <span>SSL / TLS:</span>
                <span className="text-emerald-400">Let's Encrypt Auto</span>
              </div>
              <div className="flex justify-between py-1.5">
                <span>Env Variables:</span>
                <span className="text-foreground">{envVars.length} defined</span>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </div>
  );
}
