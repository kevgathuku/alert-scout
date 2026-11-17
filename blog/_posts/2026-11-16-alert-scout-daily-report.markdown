---
layout: post
title: "Alert Scout Daily Report - 2026-11-16"
date: 2026-11-16 00:00:00 +0000
categories: alerts
---

## Summary

**Total Alerts:** 25

**Rules Matched:** 6

---

## Rule: clojure

**Matches:** 3

### Asynchronous Programming and Scala

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2017/01/30/asynchronous-programming-scala/?pk_campaign=rss](https://alexn.org/blog/2017/01/30/asynchronous-programming-scala/?pk_campaign=rss)
- **Published:** 2017-01-30 00:00

**Matched Content:**

- **[Content]** ... not necessarily multithreading. Remember that in **Java**script the majority of all I/O actions (input or...
- **[Content]** ... But no kernel-level multithreading is involved, **Java**script being an N:1 multithreaded platform....
- **[Content]** ... is defined by its abstract onComplete method in **Java**’s ExecutorService.submit(Callable) in **Java**script’s EventTarget.addEventListener in Akka actors, although there the given callback is **repl**aced by the sender() reference in the Monix...


### Immutable Collections should be Your Default

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2022/10/27/immutable-collections-your-default/?pk_campaign=rss](https://alexn.org/blog/2022/10/27/immutable-collections-your-default/?pk_campaign=rss)
- **Published:** 2022-10-27 12:34

**Matched Content:**

- **[Content]** ... options For working with immutable collections: **Java**’s standard library has utilities for creating...
- **[Content]** ... are better than nothing; VAVR works well for **Java**; Guava is also a standard recommendation for **Java**; kotlinx.collections.immutable works well for Kotlin; In Scala and **Clojure**, the default collection implementations are immutable by default, a wise design choice for **JVM** languages that provide their own (maximal) standard library; Immutable.js works well for **Java**Script; Motivation Immutable collections are much...
- **[Content]** ... or with using it in HashMap implementations. **Java**’s Strings are surprisingly sane, compared with...


### Executable Scripts with Dependencies

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2026/09/17/executable-scripts-with-dependencies/?pk_campaign=rss](https://alexn.org/blog/2026/09/17/executable-scripts-with-dependencies/?pk_campaign=rss)
- **Published:** 2026-09-17 07:00

**Matched Content:**

- **[Content]** ... #!/usr/bin/env -S dotnet run

#:package System.**Command**Line@2.0.12

using System.**Command**Line;

var nameArgument = new Argument("name");
var root**Command** = new Root**Command**("Say hello");
root**Command**.Add(nameArgument);

root**Command**.SetAction(parseResult =>
{
    var name =...
- **[Content]** ... {name}!");
});

return await root**Command**.Parse(args).InvokeAsync();
 Save this as hello.cs and then: $ chmod +x ./hello.cs
$ ./hello.cs
 **Clojure** Prerequisites Needs Babashka installed. # With...
- **[Content]** ... true :require true :desc "Name to greet"}}}]
  ***command**-line-args*
  {:prog "hello" :help true})
 Save...


---

## Rule: editor-wars

**Matches:** 4

### Blogging Platform for Hackers

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2012/01/05/blogging-for-hackers/?pk_campaign=rss](https://alexn.org/blog/2012/01/05/blogging-for-hackers/?pk_campaign=rss)
- **Published:** 2012-01-05 00:00

**Matched Content:**

- **[Content]** ... my articles are written in Markdown, with **Emacs**, the most potent text **editor** ever created - think Textmate-snippets, macros,...


### Resources for Learning Scala

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2013/05/13/getting-started-with-scala/?pk_campaign=rss](https://alexn.org/blog/2013/05/13/getting-started-with-scala/?pk_campaign=rss)
- **Published:** 2013-05-13 00:00

**Matched Content:**

- **[Content]** ... started is the Scala interpreter and a good text **editor**. First, download the archive from...
- **[Content]** ... Int = 2
 You also need a good and simple text **editor** that can do syntax highlighting for Scala. Your choice should be done in this order: your existing favorite text **editor**, if you have one Sublime Text 2 for a good...
- **[Content]** ... won’t go into here, I really keep away from text-**editor**s that aren’t open-source **Vim** or **Emacs** (in combination with scala-mode2 and yasnippet). These text **editor**s are eternal and extremely productive, however if...


### Proprietary Environments are a Trap

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2022/09/22/proprietary-environments-are-a-trap/?pk_campaign=rss](https://alexn.org/blog/2022/09/22/proprietary-environments-are-a-trap/?pk_campaign=rss)
- **Published:** 2022-09-22 07:00

**Matched Content:**

- **[Content]** ... time passes, much like good wine. The Atom1 text **editor** is dead. GitHub killed it, because it was acquired by Microsoft, and Microsoft has its own **editor**, VS Code2. These **editor**s were inspired by TextMate3, a once popular, but proprietary **editor** for macOS that died, and the resurrection attempt via open sourcing failed. VS Code is right now the most popular **editor**. I don’t know if it will be around in another 20 years. But I do know that **Emacs** and Vi, both released in 1976 (46 years ago),...


### Scala 3 / No Indent

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2025/10/26/scala-3-no-indent/?pk_campaign=rss](https://alexn.org/blog/2025/10/26/scala-3-no-indent/?pk_campaign=rss)
- **Published:** 2025-10-26 08:35

**Matched Content:**

- **[Content]** ... work well, because when inserting a newline, **editor**s have the tendency to keep the indentation of the...
- **[Content]** ... wrong indentation level, and depending on the **editor**’s behavior, the first line of the pasted block...
- **[Content]** ... doesn’t matter and Scalafmt can do its job. **Editor**s now have to be smarter about jumping to the beginning and end of a block or expression. E.g., these classic **Vim** shortcuts no longer work for navigation: [{ and...


---

## Rule: rails

**Matches:** 1

### Boring Tech May Become Irrelevant Tech

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2026/03/05/boring-tech-irrelevant/?pk_campaign=rss](https://alexn.org/blog/2026/03/05/boring-tech-irrelevant/?pk_campaign=rss)
- **Published:** 2026-03-05 16:12

**Matched Content:**

- **[Content]** ... has the right skills promoted and the guard**rails**, does it matter if novices aren’t “functional...
- **[Content]** ... an automated process, even when it involves **API** breakage. Frontier models are good, but they...
- **[Content]** ... a liability, as the common denominator of all **code** seen in the wild is not very good (which is why by simply telling the LLM to use FP, it will generate better **code** for the simple fact that FP **code** seen in the wild tends to be higher quality)....


---

## Rule: rails-deploy

**Matches:** 2

### Why I Find Heroku Suboptimal

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2011/10/23/why-i-find-heroku-suboptimal/?pk_campaign=rss](https://alexn.org/blog/2011/10/23/why-i-find-heroku-suboptimal/?pk_campaign=rss)
- **Published:** 2011-10-23 00:00

**Matched Content:**

- **[Content]** ... 127.0.0.1 and all that), but then you want to **deploy**. You have to get out of your comfort zone and...
- **[Content]** ... It basically allows you to avoid growing-up. The **deploy**ment itself couldn’t be simpler, and when browsing...
- **[Content]** ... You can also be smart about semi-static pages in **Rails** - you can cache the output inside the public/...


### Scala's Future

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2024/01/10/scala-future/?pk_campaign=rss](https://alexn.org/blog/2024/01/10/scala-future/?pk_campaign=rss)
- **Published:** 2024-01-10 17:37

**Matched Content:**

- **[Content]** ... of Productivity I’ve also been a Python and a **Ruby** developer, and I remember well the onslaught of **Ruby** on Python’s seemingly fragile market share. Python (and Java) fans were actually afraid of **Ruby** burgeoning on the scene, and they had good...
- **[Content]** ... despite the somewhat different philosophies. **Ruby** was fresh, **Ruby** seemed better for DSLs, and it has had “**Ruby** on **Rails**” as the killer app, which is a great showcase of what **Ruby** can do. At that time, Python fans were patting...


---

## Rule: rescript

**Matches:** 4

### On Scala, Functional Programming and Type-Classes

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2012/11/02/scala-functional-programming-type-classes/?pk_campaign=rss](https://alexn.org/blog/2012/11/02/scala-functional-programming-type-classes/?pk_campaign=rss)
- **Published:** 2012-11-02 00:00

**Matched Content:**

- **[Content]** ... job. In parallel, because I felt the need for a **Javascript** replacement, I’ve been learning Clojure too, because of the excellent Cloju**reScript**. I’ve fallen in love with both and I can’t really...
- **[Content]** ... types By that definition languages like Ruby and **Javascript** can be considered decent functional languages and...
- **[Content]** ... and data-structures; e.g. think of C++, Java, or **Javascript**) exposes a type-system that deals efficiently...


### Notes On JavaScript Client-side Development

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2012/11/07/notes-javascript-development/?pk_campaign=rss](https://alexn.org/blog/2012/11/07/notes-javascript-development/?pk_campaign=rss)
- **Published:** 2012-11-07 00:00

**Matched Content:**

- **[Title]** Notes On **JavaScript** Client-side Development
- **[Content]** Client-side **JavaScript** development can be quite overwhelming, even for...
- **[Content]** ... minifying This is probably my biggest gripe with **JavaScript**, that you can’t simply require("some.namespace")...


### What is Functional Programming?

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2017/10/15/functional-programming/?pk_campaign=rss](https://alexn.org/blog/2017/10/15/functional-programming/?pk_campaign=rss)
- **Published:** 2017-10-15 00:00


### In Defense of OOFP

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2018/02/12/in-defense-oofp/?pk_campaign=rss](https://alexn.org/blog/2018/02/12/in-defense-oofp/?pk_campaign=rss)
- **Published:** 2018-02-12 00:00


---

## Rule: rule-ai

**Matches:** 11

### Launch HN: Axonnic (YC F26) – Sleeves that track fingers and estimate force

- **Feed:** hn-frontpage
- **Link:** [https://news.ycombinator.com/item?id=50522776](https://news.ycombinator.com/item?id=50522776)
- **Published:** 2026-11-16 20:29

**Matched Content:**

- **[Content]** ... at Oxford and has research experience applying **machine learning** to medical imaging, alongside software and...


### How To Build a Naive Bayes Classifier

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2012/02/09/howto-build-naive-bayes-classifier/?pk_campaign=rss](https://alexn.org/blog/2012/02/09/howto-build-naive-bayes-classifier/?pk_campaign=rss)
- **Published:** 2012-02-09 00:00

**Matched Content:**

- **[Content]** In **machine learning** a classifier is able to predict, given an input,...


### Trusting Open-Source

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2024/09/06/trusting-open-source/?pk_campaign=rss](https://alexn.org/blog/2024/09/06/trusting-open-source/?pk_campaign=rss)
- **Published:** 2024-09-06 00:00

**Matched Content:**

- **[Content]** ... phone makers with patent lawsuits, even post-Ba**llm**er, while their “Microsoft changed” marketing...


### Outsourced Voices, Outsourced Minds

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2025/10/13/outsourced-voices-outsourced-minds/?pk_campaign=rss](https://alexn.org/blog/2025/10/13/outsourced-voices-outsourced-minds/?pk_campaign=rss)
- **Published:** 2025-10-13 05:24


### Update on Math Formulas via Copilot

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2025/10/21/update-on-math-formulas/?pk_campaign=rss](https://alexn.org/blog/2025/10/21/update-on-math-formulas/?pk_campaign=rss)
- **Published:** 2025-10-21 00:00

**Matched Content:**

- **[Content]** ... Studio Code as well. The results when using **GPT**-4.1 are very poor, which is a pity, as this is...
- **[Content]** ... number of requests on GitHub Copilot’s Pro plan. **Claude** Sonnet fared much better (at coding tasks in...


### 'AI' Sucks the Joy Out of Programming

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2025/10/27/ai-sucks-the-joy-out-of-programming/?pk_campaign=rss](https://alexn.org/blog/2025/10/27/ai-sucks-the-joy-out-of-programming/?pk_campaign=rss)
- **Published:** 2025-10-27 00:00

**Matched Content:**

- **[Content]** ... helps me get more work done. I now think that AI/**LLM**s suck the joy out of programming. I’ve used spicy...
- **[Content]** ... server-side. I’ve been experimenting enough with **LLM**/AI-driven programming to have an opinion on it....
- **[Content]** ... “shooting bullets in the dark”. When I tell an **LLM**-driven Agent to do something, it may take care of...


### Programming Languages in the Age of AI Agents

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2025/11/16/programming-languages-in-the-age-of-ai-agents/?pk_campaign=rss](https://alexn.org/blog/2025/11/16/programming-languages-in-the-age-of-ai-agents/?pk_campaign=rss)
- **Published:** 2025-11-16 06:14

**Matched Content:**

- **[Content]** ... boring chores. GitHub’s Copilot, powered by **GPT** or **Claude** Sonnet, does a reasonably good job at writing...


### How To Become a DevOps Engineer

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2025/11/26/how-to-become-a-good-devops-engineer/?pk_campaign=rss](https://alexn.org/blog/2025/11/26/how-to-become-a-good-devops-engineer/?pk_campaign=rss)
- **Published:** 2025-11-26 00:00

**Matched Content:**

- **[Content]** ... line tools, every day. Given the advent of **LLM** tools, it’s easier than ever to get answers. If...


### Copyright is Dead

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2026/03/06/copyright-is-dead/?pk_campaign=rss](https://alexn.org/blog/2026/03/06/copyright-is-dead/?pk_campaign=rss)
- **Published:** 2026-03-06 07:10

**Matched Content:**

- **[Content]** ... However, if the project’s source code was in the **LLM**’s training data, then one can argue that this...
- **[Content]** ... on its head. The story goes that Richard Sta**llm**an, the guy behind the GPL license and the FSF,...


### AI Has No Wisdom and Neither Will You

- **Feed:** alexn-blog
- **Link:** [https://alexn.org/blog/2026/09/22/ai-has-no-wisdom-and-neither-will-you/?pk_campaign=rss](https://alexn.org/blog/2026/09/22/ai-has-no-wisdom-and-neither-will-you/?pk_campaign=rss)
- **Published:** 2026-09-22 06:10

**Matched Content:**

- **[Content]** ... for coding. Yikes! Don’t get me wrong, I think **LLM**s are a great tool. I’m no Luddite, I’ve...
- **[Content]** ... my colleagues what I’ve learned. I gladly use **LLM**s to take care of all the boring, soul-sucking...
- **[Content]** ... at scale, everything we do is automation, **LLM**s are not the only means for it, and depending on...


### pumice 0.2.0

- **Feed:** planet-clojure
- **Link:** [https://kirahowe.com/2026/nov/15/introducing-pumice](https://kirahowe.com/2026/nov/15/introducing-pumice)
- **Published:** 2026-11-16 03:34

**Matched Content:**

- **[Content]** ... keep them working. The repo includes a skill for **Claude** Code in contrib/skills/pumice, and the README has...
