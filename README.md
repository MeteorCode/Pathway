MeteorCode Pathway
==================

[![Build Status](https://travis-ci.org/MeteorCode/Pathway.svg?branch=master)](https://travis-ci.org/MeteorCode/Pathway) 
[![Coverage Status](https://coveralls.io/repos/MeteorCode/Pathway/badge.svg?branch=master)](https://coveralls.io/r/MeteorCode/Pathway?branch=master) 
[![Codacy Badge](https://www.codacy.com/project/badge/9b34c328354647e3b799f6880d1b28e0)](https://www.codacy.com/app/MeteorCode-Labs/Pathway) 
[![GitHub release](https://img.shields.io/github/release/MeteorCode/pathway.svg?style=flat)](https://github.com/MeteorCode/Pathway/releases) 
[![Latest JavaDoc](https://img.shields.io/badge/javadoc-latest-brightgreen.svg?style=flat)](http://meteorcode.github.io/Pathway/javadoc-latest/) 
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](LICENSE)

[Documentation & Tutorials](https://github.com/MeteorCode/Pathway/wiki) |  [JavaDoc](http://meteorcode.github.io/Pathway/javadoc-latest/) | [MeteorCode Labs](https://www.meteorcodelabs.com)

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

These features are currently in Beta form, and are therefore not yet entirely complete.

  + **File I/O**: Pathway's FileHandle system abstracts away file input and output, including support for cross-platform file I/O and transparent access to the contents of Zip/Jar archives. This module's architecture, focused on pure abstraction, allows you to mount nearly any kind of vaguely file shaped system into the game, including for example databases or HTTP connections, as long as you can implement the FileHandle API for that system.
  + **Modules and Modpacks**: Pathway is designed to be highly modular, and support for game mods and expansion packs is built-in. This allows developers to release new content for Pathway games easily, and for players to create mods to customize the game to their liking. The nature of the File I/O subsystem also makes incremental updates a given, simply by supplying
a module with the relevant files changed. **Please note the Module system is not yet finished in this early beta release**

Future Work
-----------

Pathway intends to eventually include a great deal of helper and game related functions, hopefully eventually rivaling
the feature sets of other modern game engines and SDKs. To that end, planned features include:

  + **Multiplayer**: Pathway hopes to support multi-player games right out of the box for any games and modules which
utilize the Pathway API.
  + **Zero-Load**: Pathway will eventually include an intelligent, dependency-aware asset management system built with games in mind. The *Zero-Load* architecture will then, if used correctly, enable games to be virtually load-screen free at almost any scale with minimal work from the developer.
