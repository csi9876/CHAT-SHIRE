import api from "./api";

type FileInfo = {
  url: string;
  thumbnail: string;
};
//####### 에러 게시판

// 에러 전체 불러오기
export const getErrors = (projectId: string) =>
  api.get(`/projects/${projectId}/posts`);

// 에러 단일 상세 불러오기
export const getErrorDetail = (postId: number) => api.get(`/posts/${postId}`);

// 에러 작성
export const postError = (
  projectId: string,
  title: string,
  content: string,
  skillName: string[],
  attachedFileInfos: FileInfo[]
) =>
  api.post(`/projects/${projectId}/posts`, {
    title,
    content,
    skillName,
    attachedFileInfos,
  });

// 에러 수정
export const updateError = (
  postId: number,
  title: string,
  content: string,
  skillName: [],
  attachedFileInfos: []
) =>
  api.patch(`/posts/${postId}`, {
    title,
    content,
    skillName,
    attachedFileInfos,
  });

// 에러 삭제
export const deleteError = (postId: number) => api.delete(`/posts/${postId}`);

// 에러 게시글 댓글 작성
export const postErrorComent = (postId: number, content: string) =>
  api.post(`/posts/${postId}/replies`, { content });

// 에러 게시글 댓글 수정
export const updateErrorComent = (replyId: string, content: string) =>
  api.put(`/replies/${replyId}`, { content });

// 에러 게시글 댓글 삭제
export const deleteErrorComent = (replyId: string) =>
  api.delete(`/replies/${replyId}`);

// 에러 단일 상세 불러오기
export const searchErrSkillName = (projectId: string, skillName: string) =>
  api.get(`/projects/${projectId}/skill/${skillName}`);

// 에러 단일 상세 불러오기
export const searchErrConent = (projectId: string, content: String) =>
  api.get(`/projects/${projectId}/content/${content}`);
