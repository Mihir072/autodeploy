import apiClient from "./client";
import { Deployment, ApiResponse } from "@/types";

export interface CreateDeploymentRequest {
  projectId: string;
  buildId?: string;
  branch?: string;
  commitSha?: string;
}

export const deploymentsApi = {
  getDeploymentsByProject: async (projectId: string): Promise<Deployment[]> => {
    const response = await apiClient.get<ApiResponse<Deployment[]>>(`/api/deployments?projectId=${projectId}`);
    return response.data.data;
  },

  getDeploymentById: async (id: string): Promise<Deployment> => {
    const response = await apiClient.get<ApiResponse<Deployment>>(`/api/deployments/${id}`);
    return response.data.data;
  },

  createDeployment: async (data: CreateDeploymentRequest): Promise<Deployment> => {
    const response = await apiClient.post<ApiResponse<Deployment>>("/api/deployments", data);
    return response.data.data;
  },

  rollback: async (deploymentId: string): Promise<Deployment> => {
    const response = await apiClient.post<ApiResponse<Deployment>>(`/api/deployments/${deploymentId}/rollback`);
    return response.data.data;
  },
};
