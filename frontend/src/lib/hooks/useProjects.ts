import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { projectsApi, CreateProjectRequest, UpdateProjectRequest, GitHubBranch } from "@/lib/api/projects";
import { deploymentsApi, CreateDeploymentRequest } from "@/lib/api/deployments";
import { domainsApi, AddDomainRequest } from "@/lib/api/domains";
import { buildsApi, TriggerBuildRequest } from "@/lib/api/builds";
import { Project, Deployment, DomainRecord, GitHubRepository } from "@/types";

export function useProjects() {
  const queryClient = useQueryClient();

  // Projects list
  const projectsQuery = useQuery<Project[]>({
    queryKey: ["projects"],
    queryFn: async () => {
      return await projectsApi.getProjects();
    },
    retry: 1,
  });

  // Single project detail
  const useProject = (id?: string) =>
    useQuery<Project>({
      queryKey: ["project", id],
      queryFn: async () => {
        if (!id) throw new Error("Project ID required");
        return await projectsApi.getProjectById(id);
      },
      enabled: !!id,
      retry: 1,
    });

  // GitHub repositories list for import flow
  const reposQuery = useQuery<GitHubRepository[]>({
    queryKey: ["github-repos"],
    queryFn: async () => {
      return await projectsApi.getGitHubRepos();
    },
  });

  // GitHub branches for a repository
  const useBranches = (repoFullName?: string) =>
    useQuery<GitHubBranch[]>({
      queryKey: ["github-branches", repoFullName],
      queryFn: async () => {
        if (!repoFullName) return [];
        return await projectsApi.getGitHubBranches(repoFullName);
      },
      enabled: !!repoFullName,
    });

  // Project deployments list
  const useDeployments = (projectId?: string) =>
    useQuery<Deployment[]>({
      queryKey: ["deployments", projectId],
      queryFn: async () => {
        if (!projectId) return [];
        return await deploymentsApi.getDeploymentsByProject(projectId);
      },
      enabled: !!projectId,
      refetchInterval: (query) => {
        const data = query.state.data;
        const hasActive = data?.some((d) => d.status === "BUILDING" || d.status === "DEPLOYING");
        return hasActive ? 2000 : 8000;
      },
    });

  // Single deployment detail
  const useDeployment = (deploymentId?: string) =>
    useQuery<Deployment>({
      queryKey: ["deployment", deploymentId],
      queryFn: async () => {
        if (!deploymentId) throw new Error("Deployment ID required");
        return await deploymentsApi.getDeploymentById(deploymentId);
      },
      enabled: !!deploymentId,
      retry: 1,
      refetchInterval: (query) => {
        const data = query.state.data;
        return data?.status === "BUILDING" || data?.status === "DEPLOYING" ? 2000 : false;
      },
    });

  // Project custom domains
  const useDomains = (projectId?: string) =>
    useQuery<DomainRecord[]>({
      queryKey: ["domains", projectId],
      queryFn: async () => {
        if (!projectId) return [];
        return await domainsApi.getDomainsByProject(projectId);
      },
      enabled: !!projectId,
    });

  // Mutations
  const createProjectMutation = useMutation({
    mutationFn: (data: CreateProjectRequest) => projectsApi.createProject(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });

  const updateProjectMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateProjectRequest }) =>
      projectsApi.updateProject(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
      queryClient.invalidateQueries({ queryKey: ["project", variables.id] });
    },
  });

  const deleteProjectMutation = useMutation({
    mutationFn: (id: string) => projectsApi.deleteProject(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });

  const triggerBuildMutation = useMutation({
    mutationFn: (data: TriggerBuildRequest) => buildsApi.triggerBuild(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["deployments", variables.projectId] });
      queryClient.invalidateQueries({ queryKey: ["project", variables.projectId] });
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });

  const triggerDeploymentMutation = useMutation({
    mutationFn: (data: CreateDeploymentRequest) => deploymentsApi.createDeployment(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["deployments", variables.projectId] });
      queryClient.invalidateQueries({ queryKey: ["project", variables.projectId] });
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });

  const rollbackMutation = useMutation({
    mutationFn: (deploymentId: string) => deploymentsApi.rollback(deploymentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deployments"] });
      queryClient.invalidateQueries({ queryKey: ["projects"] });
    },
  });

  const addDomainMutation = useMutation({
    mutationFn: (data: AddDomainRequest) => domainsApi.addDomain(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ["domains", variables.projectId] });
    },
  });

  const verifyDomainMutation = useMutation({
    mutationFn: (id: string) => domainsApi.verifyDomain(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["domains"] });
    },
  });

  const deleteDomainMutation = useMutation({
    mutationFn: (id: string) => domainsApi.deleteDomain(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["domains"] });
    },
  });

  return {
    projectsQuery,
    reposQuery,
    useProject,
    useBranches,
    useDeployments,
    useDeployment,
    useDomains,
    createProjectMutation,
    updateProjectMutation,
    deleteProjectMutation,
    triggerBuildMutation,
    triggerDeploymentMutation,
    rollbackMutation,
    addDomainMutation,
    verifyDomainMutation,
    deleteDomainMutation,
  };
}
