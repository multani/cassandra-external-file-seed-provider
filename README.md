# A Cassandra seed provider using a dedicated file

This is a simple Cassandra seed provider that fetches the list of seeds from an externally managed file, containing one seed address per-line.

You can use it by configuring Cassandra with:
```yaml
seed_provider:
  - class_name: info.multani.cassandra.ExternalFileSeedProvider
    parameters:
      - filename: /etc/cassandra/seeds.list
```

You can use which ever tool you want to keep the content of the seeds file up-to-date. The file is regularly read to fetch the latest seeds:

```
$ cat /etc/cassandra/seeds.list
# A list of Cassandra seeds

# The first seed
172.20.15.6

# Another seed
172.20.15.7

172.20.15.8 # end of line comments are stripped
```

The provider skips empty lines, and everything that comes after a `#` comment character.

## How to use?

* Download [a `.jar` release](https://github.com/multani/cassandra-external-file-seed-provider/releases)
* Save the `.jar` file in the `lib/` directory of your Cassandra installation
* Configure Cassandra as shown above
* Keep your new seeds file updated!
