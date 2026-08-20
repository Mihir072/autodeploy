import apiClient from "./client";
import { Build, ApiResponse } from "@/types";

export interface TriggerBuildRequest {
  projectId: string;
  branch?: string;
  commitSha?: string;
}

export const buildsApi = {
  triggerBuild: async (data: TriggerBuildRequest): Promise<Build> => {
    const response = await apiClient.post<ApiResponse<Build>>("/api/builds/trigger", data);
    return response.data.data;
  },

  getBuildById: async (id: string): Promise<Build> => {
    const response = await apiClient.get<ApiResponse<Build>>(`/api/builds/${id}`);
    return response.data.data;
  },

  getBuildsByProject: async (projectId: string): Promise<Build[]> => {
    const response = await apiClient.get<ApiResponse<Build[]>>(`/api/builds?projectId=${projectId}`);
    return response.data.data;
  },

  getBuildLogsSseUrl: (buildId: string): string => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:9090";
    return `${baseUrl}/api/builds/${buildId}/logs`;
  },
};
