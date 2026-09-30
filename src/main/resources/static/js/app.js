const { createApp } = Vue;

createApp({
    data() {
        return {
            // 登录相关
            isLoggedIn: false,
            loginForm: { username: '', password: '' },
            loginError: '',
            loginLoading: false,
            authToken: localStorage.getItem('authToken') || '',

            currentView: 'apis',
            projects: [],
            currentProjectId: parseInt(localStorage.getItem('currentProjectId') || '1'),
            apis: [],
            records: [],
            filterName: '',
            filterPath: '',
            searchName: '',
            searchPath: '',
            showCreateForm: false,
            showEditForm: false,
            editingId: null,
            formData: {
                name: '',
                method: 'GET',
                path: '',
                responseStatus: 200,
                responseType: 'static',
                responseHeaders: '',
                responseBody: '',
                responseFilePath: '',
                responseFileName: '',
                responseContentType: '',
                dynamicRules: [],
                delayMs: 0,
                errorType: '',
                description: ''
            },
            uploading: false,
            uploadedFileName: '',
            // 项目相关
            showCreateProjectForm: false,
            newProjectName: '',
            newProjectDesc: '',
            showEditProjectForm: false,
            editingProjectId: null,
            editingProjectName: '',
            editingProjectDesc: '',
            // 确认弹窗
            confirmDialog: {
                show: false,
                title: '',
                message: '',
                onConfirm: null
            },
            // 请求记录分页
            recordPage: 1,
            recordPageSize: 20,
            recordTotal: 0,
            recordTotalPages: 0,
            recordSearchPath: '',
            recordSearchName: '',
            recordFilterPath: '',
            recordFilterName: '',
            toast: { show: false, message: '', type: 'success' }
        };
    },
    computed: {
        filteredApis() {
            return this.apis.filter(api => {
                const matchName = !this.filterName || api.name.toLowerCase().includes(this.filterName.toLowerCase());
                const matchPath = !this.filterPath || api.path.includes(this.filterPath);
                return matchName && matchPath;
            });
        },
        responseBodyPlaceholder() {
            return '{"code": 200, "data": []}';
        }
    },
    methods: {
        // ===== 项目相关 =====
        async loadProjects() {
            try {
                const resp = await this.authFetch('/api/admin/projects');
                const data = await resp.json();
                this.projects = data.data || [];
                // 确保当前项目存在
                if (this.projects.length > 0 && !this.projects.find(p => p.id === this.currentProjectId)) {
                    this.currentProjectId = this.projects[0].id;
                }
            } catch (e) {
                this.showToast('加载项目列表失败', 'error');
            }
        },
        switchProject(projectId) {
            this.currentProjectId = projectId;
            this.currentView = 'apis';
            localStorage.setItem('currentProjectId', projectId);
            this.loadApis();
        },
        switchView(view) {
            this.currentView = view;
            if (view === 'records') {
                this.currentProjectId = null;
                this.recordPage = 1;
                this.loadRecords();
            } else if (view === 'apis') {
                // 切回接口页面时，确保有选中项目
                if (!this.currentProjectId && this.projects.length > 0) {
                    this.currentProjectId = this.projects[0].id;
                    localStorage.setItem('currentProjectId', this.currentProjectId);
                    this.loadApis();
                }
            }
        },
        openCreateProject() {
            this.newProjectName = '';
            this.newProjectDesc = '';
            this.showCreateProjectForm = true;
        },
        async createProject() {
            if (!this.newProjectName.trim()) {
                this.showToast('请输入项目名称', 'warning');
                return;
            }
            try {
                const resp = await this.authFetch('/api/admin/projects', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name: this.newProjectName, description: this.newProjectDesc })
                });
                const data = await resp.json();
                if (data.code === 200) {
                    this.showToast('项目创建成功', 'success');
                    this.showCreateProjectForm = false;
                    await this.loadProjects();
                    this.currentProjectId = data.data.id;
                    localStorage.setItem('currentProjectId', data.data.id);
                    await this.loadApis();
                } else {
                    this.showToast(data.message || '创建失败', 'error');
                }
            } catch (e) {
                this.showToast('创建失败: ' + e.message, 'error');
            }
        },
        openEditProject(project) {
            this.editingProjectId = project.id;
            this.editingProjectName = project.name;
            this.editingProjectDesc = project.description || '';
            this.showEditProjectForm = true;
        },
        async saveEditProject() {
            if (!this.editingProjectName.trim()) {
                this.showToast('项目名称不能为空', 'warning');
                return;
            }
            try {
                const project = this.projects.find(p => p.id === this.editingProjectId);
                if (!project) return;
                const resp = await this.authFetch(`/api/admin/projects/${project.id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name: this.editingProjectName, description: this.editingProjectDesc })
                });
                const data = await resp.json();
                if (data.code === 200) {
                    project.name = this.editingProjectName;
                    project.description = this.editingProjectDesc;
                    this.showToast('保存成功', 'success');
                    this.showEditProjectForm = false;
                } else {
                    this.showToast(data.message || '保存失败', 'error');
                }
            } catch (e) {
                this.showToast('保存失败: ' + e.message, 'error');
            }
        },
        showConfirm(title, message, onConfirm) {
            this.confirmDialog = {
                show: true,
                title: title,
                message: message,
                onConfirm: onConfirm
            };
        },
        confirmDeleteProject(project) {
            this.showConfirm('删除项目', `确定删除项目「${project.name}」吗？该项目下的接口不会被删除，但会归入默认项目。`, async () => {
                try {
                    const resp = await this.authFetch(`/api/admin/projects/${project.id}`, { method: 'DELETE' });
                    const data = await resp.json();
                    if (data.code === 200) {
                        this.showToast('项目已删除', 'success');
                        this.projects = this.projects.filter(p => p.id !== project.id);
                        if (this.currentProjectId === project.id) {
                            this.currentProjectId = 1;
                            localStorage.setItem('currentProjectId', '1');
                            await this.loadApis();
                        }
                    } else {
                        this.showToast(data.message || '删除失败', 'error');
                    }
                } catch (e) {
                    this.showToast('删除失败: ' + e.message, 'error');
                }
            });
        },

        // ===== API 相关 =====
        async loadApis() {
            try {
                const resp = await this.authFetch(`/api/admin/mock-apis?projectId=${this.currentProjectId}`);
                const data = await resp.json();
                this.apis = data.data || [];
            } catch (e) {
                this.showToast('加载接口列表失败', 'error');
            }
        },
        doSearch() {
            this.filterName = this.searchName;
            this.filterPath = this.searchPath;
        },
        resetSearch() {
            this.searchName = '';
            this.searchPath = '';
            this.filterName = '';
            this.filterPath = '';
        },
        async loadRecords() {
            try {
                const offset = (this.recordPage - 1) * this.recordPageSize;
                let url = `/api/admin/records?limit=${this.recordPageSize}&offset=${offset}`;
                if (this.recordFilterPath) {
                    url += `&searchPath=${encodeURIComponent(this.recordFilterPath)}`;
                }
                if (this.recordFilterName) {
                    url += `&searchName=${encodeURIComponent(this.recordFilterName)}`;
                }
                const resp = await this.authFetch(url);
                const data = await resp.json();
                // 后端返回 { data: [...], total: xxx }
                const result = data.data || {};
                this.records = result.data || [];
                this.recordTotal = result.total || 0;
                this.recordTotalPages = Math.ceil(this.recordTotal / this.recordPageSize);
            } catch (e) {
                this.showToast('加载请求记录失败', 'error');
            }
        },
        doRecordSearch() {
            this.recordFilterPath = this.recordSearchPath;
            this.recordFilterName = this.recordSearchName;
            this.recordPage = 1;
            this.loadRecords();
        },
        resetRecordSearch() {
            this.recordSearchPath = '';
            this.recordSearchName = '';
            this.recordFilterPath = '';
            this.recordFilterName = '';
            this.recordPage = 1;
            this.loadRecords();
        },
        openCreateForm() {
            this.formData.projectId = this.currentProjectId;
            this.showCreateForm = true;
        },
        async toggleApi(api) {
            try {
                const resp = await this.authFetch(`/api/admin/mock-apis/${api.id}/toggle`, { method: 'PATCH' });
                const data = await resp.json();
                if (data.code === 200) {
                    api.enabled = data.data.enabled;
                    this.showToast(api.enabled ? '已启用' : '已禁用', 'success');
                }
            } catch (e) {
                this.showToast('操作失败', 'error');
            }
        },
        deleteApi(api) {
            this.showConfirm('删除接口', `确定删除 ${api.method} ${api.path} 吗？`, async () => {
                try {
                    const resp = await this.authFetch(`/api/admin/mock-apis/${api.id}`, { method: 'DELETE' });
                    const data = await resp.json();
                    if (data.code === 200) {
                        this.apis = this.apis.filter(a => a.id !== api.id);
                        this.showToast('删除成功', 'success');
                    } else {
                        this.showToast(data.message || '删除失败', 'error');
                    }
                } catch (e) {
                    this.showToast('删除失败: ' + e.message, 'error');
                }
            });
        },
        editApi(api) {
            this.editingId = api.id;
            // 解析 dynamicRules JSON
            let dynamicRules = [];
            if (api.dynamicRules) {
                try {
                    dynamicRules = JSON.parse(api.dynamicRules);
                } catch (e) {
                    dynamicRules = [];
                }
            }
            // 确保至少有一条规则
            if (dynamicRules.length === 0) {
                dynamicRules = [{ conditionField: '', conditionValue: '', responseBody: '', responseStatus: 200, isDefault: true }];
            }
            this.formData = {
                projectId: api.projectId || this.currentProjectId,
                name: api.name,
                method: api.method,
                path: api.path,
                responseStatus: api.responseStatus,
                responseType: api.responseType || 'static',
                responseHeaders: api.responseHeaders || '',
                responseBody: api.responseBody || '',
                responseFilePath: api.responseFilePath || '',
                responseFileName: api.responseFileName || '',
                responseContentType: api.responseContentType || '',
                dynamicRules: dynamicRules,
                delayMs: api.delayMs,
                errorType: api.errorType || '',
                description: api.description || ''
            };
            // 使用后端返回的原始文件名
            this.uploadedFileName = api.responseFileName || '';
            this.showEditForm = true;
        },
        async saveApi() {
            if (!this.formData.name || !this.formData.path) {
                this.showToast('请填写接口名称和路径', 'warning');
                return;
            }
            try {
                // 构建提交数据
                const submitData = { ...this.formData };
                // 新建时自动关联当前项目
                if (!submitData.projectId) {
                    submitData.projectId = this.currentProjectId;
                }
                // 动态模式下，将规则数组序列化为 JSON
                if (submitData.responseType === 'dynamic') {
                    submitData.dynamicRules = JSON.stringify(submitData.dynamicRules);
                } else {
                    submitData.dynamicRules = null;
                }

                const url = this.showEditForm
                    ? `/api/admin/mock-apis/${this.editingId}`
                    : '/api/admin/mock-apis';
                const method = this.showEditForm ? 'PUT' : 'POST';
                const resp = await this.authFetch(url, {
                    method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(submitData)
                });
                const data = await resp.json();
                if (data.code === 200) {
                    this.showToast(this.showEditForm ? '保存成功' : '创建成功', 'success');
                    this.closeForm();
                    await this.loadApis();
                } else {
                    this.showToast(data.message || '操作失败', 'error');
                }
            } catch (e) {
                this.showToast('操作失败: ' + e.message, 'error');
            }
        },
        closeForm() {
            this.showCreateForm = false;
            this.showEditForm = false;
            this.editingId = null;
            this.uploading = false;
            this.uploadedFileName = '';
            this.formData = {
                name: '', method: 'GET', path: '',
                responseStatus: 200, responseType: 'static',
                responseHeaders: '', responseBody: '',
                responseFilePath: '', responseFileName: '', responseContentType: '',
                dynamicRules: [],
                delayMs: 0, errorType: '', description: ''
            };
        },
        // ===== 动态规则管理 =====
        addRule() {
            this.formData.dynamicRules.push({
                conditionField: '',
                conditionValue: '',
                responseBody: '',
                responseStatus: 200,
                isDefault: false
            });
        },
        removeRule(index) {
            this.formData.dynamicRules.splice(index, 1);
        },
        onDefaultChange(index) {
            // 确保只有一个默认规则
            const rules = this.formData.dynamicRules;
            if (rules[index].isDefault) {
                for (let i = 0; i < rules.length; i++) {
                    if (i !== index) {
                        rules[i].isDefault = false;
                    }
                }
            }
        },
        async handleFileUpload(event) {
            const file = event.target.files[0];
            if (!file) return;

            this.uploading = true;
            this.uploadedFileName = file.name;

            try {
                const formData = new FormData();
                formData.append('file', file);

                const resp = await this.authFetch('/api/admin/upload', {
                    method: 'POST',
                    body: formData
                });
                const data = await resp.json();

                if (data.code === 200) {
                    this.formData.responseFilePath = data.data.filePath;
                    this.formData.responseFileName = data.data.fileName;
                    this.formData.responseContentType = data.data.contentType || this.formData.responseContentType;
                    // 使用后端返回的原始文件名
                    this.uploadedFileName = data.data.fileName || file.name;
                    this.showToast('文件上传成功', 'success');
                } else {
                    this.showToast('上传失败: ' + data.message, 'error');
                }
            } catch (e) {
                this.showToast('上传失败: ' + e.message, 'error');
            } finally {
                this.uploading = false;
            }
        },
        formatTime(dateTimeStr) {
            if (!dateTimeStr) return '';
            // 处理 ISO 格式时间（带 T）和 SQLite 格式时间
            const str = dateTimeStr.replace('T', ' ');
            // 如果是 SQLite 格式 "2026-09-24 15:30:00"，直接返回
            // 如果是 ISO 格式 "2026-09-24T15:30:00.000"，去掉毫秒
            return str.substring(0, 19);
        },
        extractFileName(filePath) {
            if (!filePath) return '';
            const parts = filePath.replace(/\\/g, '/').split('/');
            return parts[parts.length - 1];
        },
        showToast(message, type = 'success') {
            this.toast = { show: true, message, type };
            setTimeout(() => { this.toast.show = false; }, 3000);
        },
        // ===== 登录相关 =====
        async checkLoginStatus() {
            const token = localStorage.getItem('authToken');
            if (!token) {
                this.isLoggedIn = false;
                return;
            }
            try {
                const resp = await fetch('/api/auth/check', {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                if (resp.ok) {
                    this.isLoggedIn = true;
                    this.authToken = token;
                    this.loadProjects().then(() => { this.loadApis(); });
                } else {
                    this.isLoggedIn = false;
                    localStorage.removeItem('authToken');
                }
            } catch (e) {
                this.isLoggedIn = false;
            }
        },
        async doLogin() {
            if (!this.loginForm.username || !this.loginForm.password) {
                this.loginError = '请输入用户名和密码';
                return;
            }
            this.loginLoading = true;
            this.loginError = '';
            try {
                const resp = await fetch('/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.loginForm)
                });
                const data = await resp.json();
                if (data.code === 200) {
                    this.authToken = data.data.token;
                    localStorage.setItem('authToken', this.authToken);
                    this.isLoggedIn = true;
                    this.loginForm = { username: '', password: '' };
                    this.loadProjects().then(() => { this.loadApis(); });
                } else {
                    this.loginError = data.message || '登录失败';
                }
            } catch (e) {
                this.loginError = '网络错误，请重试';
            } finally {
                this.loginLoading = false;
            }
        },
        async doLogout() {
            try {
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    headers: { 'Authorization': 'Bearer ' + this.authToken }
                });
            } catch (e) {}
            localStorage.removeItem('authToken');
            this.authToken = '';
            this.isLoggedIn = false;
            this.loginForm = { username: '', password: '' };
        },
        /** 带 Token 的 fetch 封装 */
        async authFetch(url, options = {}) {
            const headers = options.headers || {};
            headers['Authorization'] = 'Bearer ' + this.authToken;
            options.headers = headers;
            const resp = await fetch(url, options);
            if (resp.status === 401) {
                // Token 过期，跳转登录页
                localStorage.removeItem('authToken');
                this.authToken = '';
                this.isLoggedIn = false;
                return null;
            }
            return resp;
        }
    },
    mounted() {
        this.checkLoginStatus();
    }
}).mount('#app');
