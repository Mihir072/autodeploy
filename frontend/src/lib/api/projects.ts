import apiClient from "./client";
import { Project, GitHubRepository, ApiResponse, EnvironmentVariable } from "@/types";

export interface CreateProjectRequest {
  name: string;
  repositoryUrl: string;
  repositoryName: string;
  branch: string;
  buildCommand?: string;
  outputDirectory?: string;
  dockerfilePath?: string;
  environmentVariables?: EnvironmentVariable[];
}

export interface UpdateProjectRequest {
  name?: string;
  branch?: string;
  buildCommand?: string;
  outputDirectory?: string;
  dockerfilePath?: string;
  environmentVariables?: EnvironmentVariable[];
}

export interface GitHubBranch {
  name: string;
  commitSha?: string;
}

export const projectsApi = {
  getProjects: async (): Promise<Project[]> => {
    const response = await apiClient.get<ApiResponse<Project[]>>("/api/projects");
    return response.data.data;
  },

  getProjectById: async (id: string): Promise<Project> => {
    const response = await apiClient.get<ApiResponse<Project>>(`/api/projects/${id}`);
    return response.data.data;
  },

  createProject: async (data: CreateProjectRequest): Promise<Project> => {
    const response = await apiClient.post<ApiResponse<Project>>("/api/projects", data);
    return response.data.data;
  },

  updateProject: async (id: string, data: UpdateProjectRequest): Promise<Project> => {
    const response = await apiClient.put<ApiResponse<Project>>(`/api/projects/${id}`, data);
    return response.data.data;
  },

  deleteProject: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/projects/${id}`);
  },

  getGitHubRepos: async (): Promise<GitHubRepository[]> => {
    const response = await apiClient.get<ApiResponse<GitHubRepository[]>>("/api/projects/github/repos");
    return response.data.data;
  },

  getGitHubBranches: async (repoFullName: string): Promise<GitHubBranch[]> => {
    const response = await apiClient.get<ApiResponse<GitHubBranch[]>>(
      `/api/projects/github/branches?repo=${encodeURIComponent(repoFullName)}`
    );
    return response.data.data;
  },
};
