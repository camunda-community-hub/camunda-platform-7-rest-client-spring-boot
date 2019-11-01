## Docs built with Orchid

### To build

```bash
./mvnw -f docs orchid:build
```

### To Serve

```bash
./mvnw -f docs -Pserve-docs
```


### To Publish

```bash
./mvnw -f docs -Pdeploy-docs -DgithubToken=GITHUB_TOKEN
```
