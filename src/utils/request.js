import axios from "axios";

const request = axios.create({
  baseURL: "http://47.115.163.178:8080",
  timeout: 5000
});

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const loginUser = JSON.parse(localStorage.getItem("loginUser"))
    // console.log(loginUser);

    if (loginUser && loginUser.token) {
      config.headers.token = loginUser.token;
    }
    // console.log("最终发送的 headers =", config.headers)
    return config;   // ✔ 必须返回 config
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    console.log(response.data);
    
    return response.data;  // ✔ 这里才返回 data
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default request;
