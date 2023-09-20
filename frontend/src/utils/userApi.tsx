import api from "./api";

// ##### 유저

// 깃 회원가입
export const getGit = () => api.get(`/oauth2/sign-up`);

// 유저 회원가입
export const postUser = (
  nickname: string,
  profileImage: string,
  profileColor: string,
  introduction: string,
  detailIntroduction: string,
  mySkill: string[]
) =>
  api.post("/users", {
    nickname,
    profileImage,
    profileColor,
    introduction,
    detailIntroduction,
    mySkill,
  });

// 내 정보 수정
export const updateProfile = (
  nickname: string,
  profileImage: string,
  profileColor: string,
  introduction: string,
  detailIntroduction: string,
  mySkill: string[]
) =>
  api.put(`/users`, {
    nickname,
    profileImage,
    profileColor,
    introduction,
    detailIntroduction,
    mySkill,
  });

// 내 정보 조회
export const getProfile = () => api.get(`/users`);

// 내 정보 삭제
export const deleteUser = () => api.delete(`/users`);

// 내 정보 삭제
export const allLanguage = () => api.get(`/skills`);
