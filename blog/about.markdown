---
layout: page
title: About
permalink: /about/
---

Alert Scout is an RSS/Atom feed monitoring system that watches for topics of interest across multiple feeds and generates daily alert reports.

## How it works

- **Feeds** are checked every 6 hours for new items
- **Rules** define search terms using boolean logic (must, should, must-not)
- **Alerts** are generated when feed items match rules, with relevant excerpts highlighted
- **Daily reports** are published here, consolidating the day's matches

## Currently monitoring

Topics including AI/LLM developments, Clojure ecosystem updates, and database technologies across sources like Hacker News and Planet Clojure.

## Built with

- [Clojure](https://clojure.org/) with [Leiningen](https://leiningen.org/)
- [Rome Tools](https://rometools.github.io/rome/) for feed parsing
- [Jsoup](https://jsoup.org/) for HTML text extraction
- [Jekyll](https://jekyllrb.com/) for site generation
- Automated via GitHub Actions
