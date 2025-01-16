import { createStore } from 'vuex'

export default createStore({
  state: {
    // 自定义需要全局保存的对象
    member: {}
  },
  getters: {
  },
  mutations: {
    // 对state值的修改方法
    setMember (state, _member) {
      state.member = _member
    }
  },
  actions: {
    // 异步
  },
  modules: {
    // 模块话，如果项目较大可采用
  }
})
