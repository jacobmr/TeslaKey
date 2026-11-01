# Instructions for Claude

## Artifact index (docs.reider.us)
After creating any artifact, doc, spec, or question doc, add an entry to the
master index: https://claude.ai/artifact/SF3PuPCs8zeGrvF56G9EEk
- If the ArtifactData tool is available: `set` on collection `entries` with
  title, url, folder, source ("code"), note (one line), archived=false,
  added (ISO timestamp). Reuse existing folders; archive, don't delete.
- If it isn't available: end the session by giving me the title, link,
  and a suggested folder so I can add it from the page's "Add an entry" form.
