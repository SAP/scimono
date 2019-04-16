# Contributing to SCIMono

Our aim is to build a lively community, hence, we welcome any exchange and collaboration with individuals and 
organizations interested in the use, support and extension of the open-source SCIMono Library.

Here are some of the ways you can contribute.

## Reporting Issues

Please go to our issues page: [Issues](https://github.com/SAP/scimono/issues) to find any outstanding issues or to 
report new ones.

## Contributing Code

We welcome any contributions to the SCIMono codebase. You can browse the list of open issues and contribute a fix for 
any of them or you can propose an extension to the library. Anyway, you have to be aware of some rules we have:
  
  1. You must be aware of the Apache License (which describes contributions), and you must agree to the [Contributors License Agreement](https://gist.github.com/CLAassistant/bd1ea8ec8aa0357414e8). This is common practice for most open source projects.
  
      Note: You do not need to sign the CLA until you submit your first pull request. If you have not signed the CLA before, a link to the CLA assistant is provided on the PR status page.
  
      * To make this process as simple as possible, we use the *[CLA assistant](https://cla-assistant.io/)* for individual contributions. CLA assistant is an open source tool that integrates with GitHub very well and enables a one-click-experience for accepting the CLA.
  
  1. Contributions must be compliant with the project code style, quality, and standards. 
  
### Contributor License Agreement

When you contribute anything to SCIMono, be aware that your contribution is covered by the same 
[Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0) that is applied to SCIMono itself. In particular, 
you must agree to the [Individual Contributor License Agreement](https://gist.github.com/CLAassistant/bd1ea8ec8aa0357414e8).

This applies to all contributors, including those contributing on behalf of a company. If you agree to its content, 
click on the link posted by the CLA assistant as a comment to the pull request. Click it to check the CLA, then accept 
it on the following screen if you agree to it. CLA assistant will save this decision for upcoming contributions and will
 notify you if there is any changes to the CLA in the meantime.

### Contribution Process

1. Create a fork of the SCIMono library sources. 

1. Build and run the tests. 

    We use maven to build our library and to maintain dependencies. To build the library and run the integration tests,
    just execute:
    
        ```mvn clean install```
    
    inside the parent directory.

1. Work on the change in your fork in a dedicated branch.

1. Commit and push your changes using the [squash and merge](https://help.github.com/articles/about-pull-request-merges/) feature in GitHub.

    That you should also use the squash and merge feature when additional changes are required after code review.

1. In the commit message, please describe the change as thorough as possible. 

1. If your change fixes an issue reported in GitHub, add the following line to the commit message:

     ```Fixes https://github.com/SAP/scimono/issues/(issueNumber)```

    * Do not add a colon after "Fixes", as this prevents automatic closing.
    * When your pull request number is known (for example, because you enhanced a pull request after a code review), you can also add the following line:

        ```Closes https://github.com/SAP/scimono/pull/(pullRequestNumber)```

1. Create a pull request so that we can review your change.
1. Follow the link posted by the CLA assistant to your pull request and accept it, as described above.
1. Wait for our code review and approval, possibly enhancing your change on request.
    
    Note: This may take time, depending on the required effort for reviewing, testing, and clarification. SCIMono developers are also working their regular duties.

1. After the change has been approved, we will inform you in a comment.

1. Due to internal SAP processes, your pull request cannot be merged directly into the branch. It will be merged internally, and will also immediately appear in the public repository.
1. We will close the pull request. At that point, you can delete your branch.
