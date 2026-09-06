# Sprint 4 — automatic budget alerts

Budget activity uses the current calendar date in Asia/Ho_Chi_Minh, inclusive
start_date/end_date, and an active academic term (budgets have no status field).
Every transaction create/update/delete recalculates all active budgets belonging
to that user. Each total includes only expenses in the budget's own category,
academic term and inclusive date window, independent of period_type. Income is
excluded. The threshold is strictly greater than the budget amount.

The message includes budget_id, period_type, excess amount and excess percentage
(relative to the limit, not percent consumed). The API also returns budget.id and
budget.periodType. The nearest incomplete milestone from today through today +7
inclusive in the same term is linked, otherwise milestone is null.

Transaction mutations and alert generation share one PostgreSQL transaction with
a per-user row lock. There is at most one pending budget warning per budget from
this flow. A pending warning is updated on recalculation; when spending falls to
or below the limit it is dismissed. Read/dismissed warnings remain as history;
a subsequent transaction can issue a new warning if spending is still over limit.
No timer/queue is used; recalculation is triggered by transaction mutations.

API (all JWT-protected, scoped to authenticated user):
- GET /api/v1/alerts?status=unread|new|read|dismissed (unread includes legacy new)
- PATCH /api/v1/alerts/:id/read
- PATCH /api/v1/alerts/:id/dismiss

Home loads real alerts, shows an unread banner and list, updates read state after
API success, and removes dismissed alerts from the visible list. Retrofit create
and update requests include @Body to send actual JSON.

Run the real HTTP regression suite against a running migrated backend:

```bash
API_URL=http://127.0.0.1:3004/api/v1 node backend/test/sprint4-api.mjs
```

The suite creates unique test users and leaves their fixtures for inspection.
Use a disposable database. It prints real alert responses without auth tokens.
