# What this is

A very early version of an Git LFS (https://git-lfs.github.com/) REST API interface plugin for Atlassian Stash (https://www.atlassian.com/software/stash).

# How to use this

## As a Sysadmin/User

This is a very early release! Use at your own risk!

* Install git-lfs (see https://git-lfs.github.com/ for details)
* Download (see releases) and install the plugin in your stash instance
* Init LFS and add the LFS endpoint to your repo
```
git lfs init
git config remote.origin.lfsurl "https://your.server.com/stash/rest/lfs/<project>/<repo>"
```
* Add files to track by LFS ```git lfs track "*.tar.gz"```
* Commit, push, whatever

Cloning a repo with LFS content is currently not possible sind the default LFS url is different from this plugins REST url (see TODO).

## As a plugin developer

Well, get familiar with the atlassian sdk (https://developer.atlassian.com/docs/getting-started/set-up-the-atlassian-plugin-sdk-and-build-a-project), clone the repo and go with atlas-run.

# Smallish documentation

The file to get you started is Lfs.java. Pretty much everything is in there. The Objects (LfsObject.java and BatchLfsObject.java) are direct mappings of the input JSON coming from the git-lfs client. The Models (LfsModel.java and BatchLfsModel.java) are the output/return Objects which will be mapped to JSON for the response.

# TODO

There are a lot of TODOs. I definitely can not do all of them by myself, so please contact me or just send a pull request if you have anything.

* Checksum verification of uploaded file
  * CON: High CPU load for big files
  * PRO: Especially for big files you want to have file consistency
* Validate JSON (LfsObject.validateJSON)
* Fix Hypermedia links to only include needed links?? (check if needed)
* Check batch API
  * (DONE) push
  * pull (in my tests client did not use batch even if told to do so...)
* Hook to delete the lfs repo if stash repo is deleted
* Make 404 etc. empty and not return massive stash thing
* Move path from /rest... to default LFS path ...repo.git/info/lfs
  * I am not sure if this is possible at all
* Add SSH implementation (https://github.com/github/git-lfs/tree/master/docs/api#authentication)
* Fix passing of lfsStore path everywhere (HOW???)
* Add JavaDoc
* Add tests
* Add more tests
