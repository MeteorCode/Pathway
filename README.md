MeteorCode Pathway
==================

[![Build Status](https://travis-ci.org/MeteorCode/Pathway.svg?branch=master)](https://travis-ci.org/MeteorCode/Pathway)| [Latest Build](https://jenkins.meteorcodelabs.com/job/Pathway/lastSuccessfulBuild/) | [Latest JavaDoc](https://jenkins.meteorcodelabs.com/job/Pathway/javadoc/) | [Source](https://gitlab.meteorcodelabs.com/meteorcode/pathway)

MeteorCode Pathway is an open-source event-driven game engine for JVM platforms. It is intended primarily for turn-based role-playing and strategy games, but is flexible enough for use in any type of game. 

Event-Driven Game Engine?
-------------------------

Pathway's core paradigm models all game behaviour as the interaction of *[Events](https://jenkins.meteorcodelabs.com/job/Pathway/javadoc/com/meteorcode/pathway/model/Event.html)* and *[Properties](https://jenkins.meteorcodelabs.com/job/Pathway/javadoc/com/meteorcode/pathway/model/Property.html)*. Events represent an event that takes place in the game. When an actor, such as a character, a monster, or the environment, does something, it fires an event onto the event stack. At the end of a turn (or in real time), events are popped off of the event stack, giving the relevant properties the chance to react to them, either by modifying the event or by firing more events onto the event stack. While this concept is fairly simple, it can model a wide range of rule systems or gameplay behaviour.

Scripting
---------

Scripts controlling the behaviour of Pathway Entities, Properties, and Events are written in [BeanShell](http://www.beanshell.org), a lightweight scripting language for Java. BeanShell should be immediately familiar to Java programmers - it's syntax is identical to Java.

While BeanShell was chosen as the primary scripting language for Pathway, Pathway's script API is highly modular and extensible, and adding support for other JVM-compatible scripting languages, such as Groovy and Jython, should be possible.

Other Features
--------------

Pathway's event engine and scripting system are its' primary features, but it also provides other helpful features to game developers.

  + **File I/O**: Pathway's FileHandle system abstracts away file input and output, including support for cross-platform file I/O and Zip/Jar archives.
  + **Modules and Modpacks**: Pathway is designed to be highly modular, and support for game mods and expansion packs is built-in. This allows developers to release new content for Pathway games easily, and for players to create mods to customize the game to their liking.
  + **Multiplayer**: Pathway supports multi-player games right out of the box.
