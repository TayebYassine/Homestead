# Migrate to new Database

Already have an active server with regions? Follow these steps to safely migrate your data.

1. Prepare the new provider by filling in its connection details in **config.yml**.
2. Save your changes and reload the configuration:
   ```
   /hsadmin reload
   ```
3. Export your data to the new provider:
   ```
   /hsadmin export [provider]
   ```
   Replace `[provider]` with your target provider (e.g., `MySQL`, `PostgreSQL`)
4. Wait for the export to complete. Watch the console for progress messages.
5. Stop your server completely.
6. Update **config.yml** to change the `provider` value to your new provider.
7. Start your server. Homestead will now use the new database!
