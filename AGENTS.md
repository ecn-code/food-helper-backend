# FoodHelper Agent Notes

Agents may only read, edit, create, delete, or otherwise operate on files under `/Users/elias/Documents/FoodHelper`. Do not inspect or modify sibling projects such as `/Users/elias/Documents/FoodHelperFront` unless the user explicitly updates this instruction.

Before reporting that tests could not be executed because Docker or Testcontainers is unavailable, read and follow the repository skills under `.codex/skills`.

For this project:

- Use `.codex/skills/testing-task-gate/SKILL.md` for any functional change.
- Use `.codex/skills/remote-docker-testcontainers/SKILL.md` when running `mvn test` or `mvn verify` requires Docker/Testcontainers.

Do not respond with a generic message like "I could not run the integration tests because the Docker daemon is not running" until you have first attempted the repository-specific Testcontainers flow from `remote-docker-testcontainers`, including the documented tunnel, environment variables, and connectivity check.
