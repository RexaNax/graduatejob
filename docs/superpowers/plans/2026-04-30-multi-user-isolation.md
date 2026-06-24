# Multi-User Isolation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a lightweight two-user isolation model where `admin` can access all files and `demo` can access only their own files, while preserving preview and share behavior.

**Architecture:** Persist file ownership with a new `user_id` column on `lfs_file`, centralize current-user/admin checks in backend helpers, and apply ownership filtering across file, trash, share, and stats flows. Keep frontend changes minimal by reusing existing login/session handling and only surfacing current identity clearly.

**Tech Stack:** Spring Boot, MyBatis-Plus, MySQL SQL init scripts, Vue 3, Element Plus, Docker Compose

---

### Task 1: Plan and Data Model Alignment

**Files:**
- Create: `C:\yjxbishe\docs\superpowers\plans\2026-04-30-multi-user-isolation.md`
- Reference: `C:\yjxbishe\docs\superpowers\specs\2026-04-30-multi-user-isolation-design.md`

- [ ] **Step 1: Confirm design scope**

Read the approved spec and ensure implementation only covers:

```text
1. admin sees all
2. demo sees only own data
3. file ownership stored on lfs_file
4. share access remains anonymous
5. no self-registration or user admin panel
```

- [ ] **Step 2: Lock impacted areas**

Implementation will touch these areas only:

```text
backend/sql
backend user/session helpers
backend file/trash/share/stats filtering
frontend login copy + header user display
```

### Task 2: Database and Entity Ownership

**Files:**
- Modify: `C:\yjxbishe\project\backend\sql\00-init-all.sql`
- Modify: `C:\yjxbishe\project\backend\sql\init_new_tables.sql`
- Modify: `C:\yjxbishe\project\backend\sql\lfs.sql`
- Modify: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\entity\File.java`

- [ ] **Step 1: Write failing ownership expectations in comments/checklist**

Target behavior:

```text
- new file/folder records must persist user_id
- historical data defaults to admin ownership
- demo seed account exists
```

- [ ] **Step 2: Add user_id to SQL schemas**

Update SQL definitions so `lfs_file` contains:

```sql
user_id BIGINT(20) DEFAULT '1' COMMENT '归属用户ID'
```

Place it near `dir_id` so ownership is easy to explain.

- [ ] **Step 3: Seed demo account**

Insert a second default user:

```sql
('demo', '<bcrypt for 123456>', '演示用户', 1)
```

Keep `admin` seed intact.

- [ ] **Step 4: Add File.userId**

Add the field:

```java
private Long userId;
```

and update the main constructor signature so new file records can carry ownership.

### Task 3: Current User Context and Login Boundary

**Files:**
- Modify: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\service\UserService.java`
- Modify: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\interceptor\GlobalInterceptor.java`
- Modify: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\controller\BaseController.java`
- Create: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\service\CurrentUserService.java`

- [ ] **Step 1: Add backend helper for current user**

Create a focused helper service that can:

```java
public Long getCurrentUserId()
public String getCurrentUsername()
public boolean isAdmin()
```

Use `HttpServletRequest` + `JwtConfig` so controllers/services don’t duplicate token parsing.

- [ ] **Step 2: Remove admin-only login restriction**

In `UserService.login`, delete the `isAdminUser(user)` gate and keep only:

```java
Assert.notNull(user, ErrorCode.USER_NOT_EXIST);
Assert.isTrue(user.getStatus() == 1, ErrorCode.USER_DISABLED);
Assert.isTrue(passwordEncoder.matches(password, user.getPassword()), ErrorCode.PASSWORD_ERROR);
```

- [ ] **Step 3: Keep admin role check available**

Retain:

```java
public boolean isAdminUser(User user)
```

because later backend filtering still needs it.

- [ ] **Step 4: Stop interceptor from blocking non-admin users**

In `GlobalInterceptor`, remove the username restriction block and only reject invalid tokens.

- [ ] **Step 5: Expose current-user helpers to controllers**

In `BaseController`, add convenience passthrough methods if useful:

```java
protected Long getCurrentUserId()
protected boolean currentUserIsAdmin()
```

backed by `CurrentUserService`.

### Task 4: File Ownership Persistence and Access Control

**Files:**
- Modify: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\service\FileService.java`
- Modify: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\mapper\FileMapper.java`

- [ ] **Step 1: Inject CurrentUserService**

Add:

```java
@Autowired
private CurrentUserService currentUserService;
```

to `FileService`.

- [ ] **Step 2: Add ownership-aware query helper**

Create a small private method in `FileService`:

```java
private void appendOwnershipFilter(LambdaQueryWrapper<File> wrapper) {
    if (!currentUserService.isAdmin()) {
        wrapper.eq(File::getUserId, currentUserService.getCurrentUserId());
    }
}
```

Use the same idea for `QueryWrapper` access where needed.

- [ ] **Step 3: Bind new uploads/folders to current user**

When creating new `File` rows in `saveFile(...)` and `dirAdd(...)`, assign:

```java
file.setUserId(currentUserService.getCurrentUserId());
```

and update constructor use accordingly.

- [ ] **Step 4: Scope list and tree queries**

Apply ownership filtering to:

```java
listByPage(...)
dirTree(...)
getFilePathList(...)
findByMd5(...)
findByMd5WithTrash(...)
```

For admin, keep global behavior unchanged.

- [ ] **Step 5: Scope ID-based operations**

Add reusable assertion helpers:

```java
private File getAccessibleFileOrThrow(Long id)
private void assertAccessibleFile(File file)
```

Use them in:

```text
getFileVoById
manualTranscode
updateFileThum
updateDuration
updateName
move
trash
trashFile
recycle
getDownloadUrl
```

- [ ] **Step 6: Add per-user stats support**

Add mapper methods:

```java
Long sumTotalFileSizeByUserId(@Param("userId") Long userId);
Long countFilesByUserId(@Param("userId") Long userId);
```

and switch `getStorageStats()` to use global values for admin and user-scoped values for demo.

### Task 5: Trash and Share Isolation

**Files:**
- Modify: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\service\FileTrashService.java`
- Modify: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\service\FileShareService.java`
- Modify: `C:\yjxbishe\project\backend\src\main\java\cn\lxinet\lfs\controller\UserController.java`

- [ ] **Step 1: Scope trash list by file ownership**

In `FileTrashService.listByPage`, after loading trash records, only retain rows whose `fileId` maps to an accessible file for normal users.

- [ ] **Step 2: Scope trash delete/recycle/clear**

Before deleting or recycling trash records, ensure each trash item maps to an accessible file unless current user is admin.

`clearAll()` should clear only the current user’s trash when not admin.

- [ ] **Step 3: Scope share creation and listing**

In `FileShareService.createShare` and `getSharesByFileId`, ensure normal users can only operate on accessible files.

- [ ] **Step 4: Scope share cancellation**

Before `removeById(shareId)`, load the share, resolve its file, and confirm ownership for normal users.

- [ ] **Step 5: Surface admin flag in user info**

In `/user/login` and `/user/info`, include a lightweight flag in returned user info:

```java
data.put("isAdmin", userService.isAdminUser(user));
```

or set it onto the response payload object/map so frontend can display role clearly.

### Task 6: Frontend Identity and Demo Guidance

**Files:**
- Modify: `C:\yjxbishe\project\frontend\src\views\login\Login.vue`
- Modify: `C:\yjxbishe\project\frontend\src\components\layouts\BaseNav.vue`

- [ ] **Step 1: Update login guidance copy**

Change the login tip to mention both demo accounts:

```text
admin / 123456：管理员，可查看全部数据
demo / 123456：演示用户，只查看自己的空间
```

- [ ] **Step 2: Show user role in header**

Enhance `BaseNav.vue` display to show:

```text
管理员
普通用户
```

based on the persisted `user.isAdmin` flag.

- [ ] **Step 3: Keep session handling unchanged**

Reuse existing `localStorage.setItem('user', JSON.stringify(result.data.user))` flow; only ensure the stored user object carries the new role flag.

### Task 7: Verification and Regression Checks

**Files:**
- Modify: `C:\yjxbishe\project\backend\src\test\java\cn\lxinet\lfs\controller\FilePreviewControllerTest.java` (only if needed)

- [ ] **Step 1: Verify preview regression still passes**

Run a focused backend test:

```powershell
docker run --rm -v C:\yjxbishe\project\backend:/app -w /app maven:3.9-eclipse-temurin-17 mvn -q -Dtest=FilePreviewControllerTest test
```

Expected: exit code `0`

- [ ] **Step 2: Verify app boots with compose**

Run:

```powershell
docker compose up -d --build
docker compose ps
```

Expected: `mysql`, `redis`, `backend`, `nginx` all `Up`

- [ ] **Step 3: Verify user login and file visibility manually**

Use HTTP checks or manual browser checks for:

```text
admin login succeeds
demo login succeeds
admin can query file list
demo can query file list without seeing admin-owned historical data
```

- [ ] **Step 4: Verify preview URL still returns 200**

Re-run the known preview request pattern for `06_封面图.png` and confirm:

```text
HTTP 200
Content-Type: image/png
```

- [ ] **Step 5: Commit**

When a git repository is available, commit with:

```bash
git add project/backend project/frontend project/deploy docs/superpowers
git commit -m "feat: add lightweight multi-user isolation"
```
