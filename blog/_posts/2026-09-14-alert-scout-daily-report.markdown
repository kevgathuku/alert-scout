---
layout: post
title: "Alert Scout Daily Report - 2026-09-14"
date: 2026-09-14 00:00:00 +0000
categories: alerts
---

## Summary

**Total Alerts:** 10

**Rules Matched:** 3

---

## Rule: clojure

**Matches:** 2

### vim-slime

- **Feed:** planet-clojure
- **Link:** [https://tarnbarford.net/journal/vimslime](https://tarnbarford.net/journal/vimslime)
- **Published:** 2026-09-14 11:33

**Matched Content:**

- **[Content]** ... or <a href="https://github.com/vim-scripts/Vim**Clojure**">something similar</a>, this is why it's...
- **[Content]** ... on the left is me in gVim writing some awful **Clojure** <a href="#footnote-1">[1]</a>. On the right is a screen buffer in which I started a **Clojure** **REPL**. When I want to try run some code I can send any vim text selection to the **REPL** in a keystroke (or two).</p> <p><img alt="vim...
- **[Content]** ... <p>It doesn't have to be a **Clojure** **REPL** either, we can send anything to a screen shell. We could run git **command**s, find, grep, sed, etc. Like with the **Clojure** **REPL** we can even interact with any terminal programs...


### Babashka 1.13.222: the conj release

- **Feed:** planet-clojure
- **Link:** [https://blog.michielborkent.nl/babashka-1.13.222.html](https://blog.michielborkent.nl/babashka-1.13.222.html)
- **Published:** 2026-09-14 23:59

**Matched Content:**

- **[Content]** ... I&aposll be giving a babashka workshop at **Clojure**/conj together with Rahul De. Hope to see you there! Dependencies without a **JVM** Babashka now resolves dependencies without a **JVM** by default. To demonstrate this, save the...
- **[Content]** ... To make sure this example runs without **Java**, we will remove it from the PATH and set **JAVA**_HOME to a non-existing directory as well: # Find bb before clearing PATH.
bb_executable=$(**command** -v bb)
env PATH= **JAVA**_HOME=/does-not-exist "$bb_executable" -Sforce...
- **[Content]** ... in the new native resolver, switch back to the **JVM** resolver: export BABASHKA_DEPS_RESOLVER=**jvm**
bb deps-example.clj
 To select the **JVM** resolver per project, set this in bb.edn: {:deps-resolver :**jvm**}
 Bundles tools.deps The native resolver is built on top of **clojure**.tools.deps. You can now use that directly from bb without adding a dependency: (require &apos[**clojure**.tools.deps :as deps]
         &apos[babashka.fs...


---

## Rule: rescript

**Matches:** 1

### Swipe Keyboard

- **Feed:** planet-clojure
- **Link:** [https://tarnbarford.net/journal/swipe](https://tarnbarford.net/journal/swipe)
- **Published:** 2026-09-14 11:33

**Matched Content:**

- **[Content]** ... <h2 id="swipe-loading">Loading<noscript>**Javascript** is Required</noscript></h2> </div> <p>I initially...
- **[Content]** ... in <a href="https://github.com/clojure/cloju**rescript**">Cloju**reScript**</a> or <a href="https://github.com/clojure/cloju**rescript**">Clojure</a>, so my code my vary from...
- **[Content]** ... to using native **Javascript** maps</a>.</p> <p>I found that <a...


---

## Rule: rule-ai

**Matches:** 7

### OpenArch – PyTorch implementations of modern LLM architectures

- **Feed:** hn
- **Link:** [https://github.com/anuj0456/OpenArch](https://github.com/anuj0456/OpenArch)
- **Published:** 2026-09-14 07:55

**Matched Content:**

- **[Title]** OpenArch – PyTorch implementations of modern **LLM** architectures


### Show HN: StemJSON – a language for LLMs to extend native mobile apps on the fly

- **Feed:** hn
- **Link:** [https://stemjson.com/](https://stemjson.com/)
- **Published:** 2026-09-14 11:26

**Matched Content:**

- **[Title]** Show HN: StemJSON – a language for **LLM**s to extend native mobile apps on the fly


### Apple's Siri AI Can Be Swapped Out for Claude, ChatGPT, Code Shows

- **Feed:** hn
- **Link:** [https://www.macrumors.com/2026/09/14/siri-can-be-swapped-out-for-chatgpt-claude/](https://www.macrumors.com/2026/09/14/siri-can-be-swapped-out-for-chatgpt-claude/)
- **Published:** 2026-09-14 12:01

**Matched Content:**

- **[Title]** Apple's Siri AI Can Be Swapped Out for **Claude**, Chat**GPT**, Code Shows


### Slow developer experience will bottleneck fast models

- **Feed:** sean-goedecke
- **Link:** [https://seangoedecke.com/slow-devex-will-bottleneck-fast-models/](https://seangoedecke.com/slow-devex-will-bottleneck-fast-models/)
- **Published:** 2026-09-14 00:00

**Matched Content:**

- **[Content]** ... that run at thousands of tokens-per-second. **GPT**-6-Astra can run at about sixty tokens per second....


### Why don't machine learning research agents overfit?

- **Feed:** hn
- **Link:** [https://www.amazon.science/blog/why-dont-machine-learning-research-agents-overfit](https://www.amazon.science/blog/why-dont-machine-learning-research-agents-overfit)
- **Published:** 2026-09-14 16:32

**Matched Content:**

- **[Title]** Why don't **machine learning** research agents overfit?


### Notes on gotchas while migrating 35kb preprompts from Opus to self-hosted Ollama

- **Feed:** hn-frontpage
- **Link:** [https://patrickmccanna.net/notes-on-migrating-large-prompts-away-from-anthropic-openai-to-self-hosted-llms/](https://patrickmccanna.net/notes-on-migrating-large-prompts-away-from-anthropic-openai-to-self-hosted-llms/)
- **Published:** 2026-09-14 13:59

**Matched Content:**

- **[Content]** ... Comments URL:...


### Show HN: Nari Qwen3-TTS and Qwen3-ASR – High accuracy, low latency and cost

- **Feed:** hn-frontpage
- **Link:** [https://narilabs.com/blog/nari-labs-leads-coval-voice-ai-benchmarks/](https://narilabs.com/blog/nari-labs-leads-coval-voice-ai-benchmarks/)
- **Published:** 2026-09-14 16:07

**Matched Content:**

- **[Content]** ... an inference problem. Existing systems such as v**LLM** / SGLang are not well suited for multimodal...
