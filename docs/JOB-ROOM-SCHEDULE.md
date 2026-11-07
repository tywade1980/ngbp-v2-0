# Job room: the task-level Gantt, the day view, time on site and Tonight's office work

Built Nov 7 2026 from the job room prototype Tyler approved ("I love it all lets push that into production along with the
tools functions logic and code you used to create it from my data"). Every job uses it the same way. Nothing in the code names
a job: every number comes from the office record through the paired phone.

## What changed in the app

| Where | What |
| --- | --- |
| `ui/screens/hocc/JobPlan.kt` | The plan engine. It reads the office schedule (`GET hub/project/{id}/schedule`) and the business day model. It lays out the estimate as the baseline, then projects the remaining work from today using real hours and a learned pace, blended with 8 hours of the estimate. It also gives one day's projected tasks and a missed day's knock-on across every job (one-man crew). Pure Kotlin, unit tested. |
| `ui/screens/hocc/Tonight.kt` | Tonight's office work, across every job and lead. It covers estimates to write (leads with nothing sent), scopes and hours to plan (tasks with no estimate, signed jobs with no start date), and re-planning each live job from today. Each item carries Caroline's prep steps, kept apart from his step. Also unit tested. |
| `ui/screens/hocc/PlanBoard.kt` | Reads every active job's plan plus the leads, estimates and the week's appointments. Sends Caroline her prep through her assist thread (prep only: she reaches no one and changes nothing). Remembers on the phone, for the day, what was sent and what he marked handled. "Start on its own" is off until he turns it on. |
| `ui/screens/hocc/ScheduleRoom.kt` | The Job schedule page has four tabs: Gantt (tap a task to set its hours, log time on it, or mark it finished), Day (projected tasks, the other jobs that day, and the missed-day box), Time on site (GPS and logged time by day, each day linked to its daily log, and hours tied to their task) and Phases (the card's earlier content, kept). Also holds the dashboard face and the Tonight's office work page. |
| `ui/screens/hocc/JobRoomScreen.kt` | The job room is a fixed dashboard. A card not opened only reads, and tapping it opens its page, where the editing lives. Adds the Tonight button in the header. The daily logs open on the day the schedule asked for. |
| `ui/screens/hocc/RoomViewModel.kt` | Adds the Gantt's writers, all through the office's existing schedule doors. Set a task's estimate (`PATCH hub/items/{id}`). Tie hours to a task (`POST hub/hours/{id}/link`). Log hours on a task (`POST projects/{id}/hours`, then the link). Mark finished (`PATCH hub/items/{id}` with `finished`), offered only once the office supports it (below). |

## One change the office needs: a finished mark on schedule items

The office's schedule items (`sched_items`) have no finished field today. `schedule.update_item` accepts only `title`, `seq`,
`est_minutes` and `phase_id`, and the hub's `ItemBody` drops unknown fields. Without a finished mark the plan still works: it
counts real hours against each estimate. But it can't tell that a task finished early, which is what pulls the finish in.

The app is ready for it. It shows **Mark finished** only when the schedule read carries `finished_at` on items. Until then,
the button doesn't appear and nothing calls the door. The box job, for the crew (add to what exists, one writer, an edit row):

1. Migration: `ALTER TABLE sched_items ADD COLUMN finished_at timestamptz NULL, ADD COLUMN finished_by text NULL;` (with a
   matching downgrade).
2. `schedule.ITEM_FIELDS` += `finished`. In `update_item`, `finished: true` sets `finished_at = now()` and `finished_by = by`,
   and `finished: false` clears both. The change is written to `sched_edits` with its before and after, like every other edit.
3. `hub_api.ItemBody` += `finished: bool | None = None`.
4. `schedule.read` already returns `i.*`, so `finished_at` reaches the app with no read change.
5. Optional: `_replan` skips laying blocks for finished items, so the baseline also stops at the finish.

Tyler tests it before it ships, as with every box change.

## Not on the phone

- A nightly box-side start of Caroline's prep, so it runs even when the app isn't opened. Today, with "start on its own"
  turned on, the prep starts when the app reads the board.
- Payment milestones on the missed-day box. The prototype showed draws moving with their phases. The record doesn't tie a
  draw to a phase, so the box shows phase and finish dates only.
