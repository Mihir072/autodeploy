import apiClient from "./client";
import { DomainRecord, ApiResponse } from "@/types";

export interface AddDomainRequest {
  projectId: string;
  domainName: string;
}

export const domainsApi = {
  getDomainsByProject: async (projectId: string): Promise<DomainRecord[]> => {
    const response = await apiClient.get<ApiResponse<DomainRecord[]>>(`/api/domains?projectId=${projectId}`);
    return response.data.data;
  },

  addDomain: async (data: AddDomainRequest): Promise<DomainRecord> => {
    const response = await apiClient.post<ApiResponse<DomainRecord>>("/api/domains", data);
    return response.data.data;
  },

  verifyDomain: async (id: string): Promise<DomainRecord> => {
    const response = await apiClient.get<ApiResponse<DomainRecord>>(`/api/domains/${id}/verify`);
    return response.data.data;
  },

  deleteDomain: async (id: string): Promise<void> => {
    await apiClient.delete(`/api/domains/${id}`);
  },
};
