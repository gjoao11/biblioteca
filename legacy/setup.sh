#!/usr/bin/env bash
set -e
[ -d app ] || git clone https://github.com/prabhakar267/library-management-system app
git -C app checkout 64e436a
