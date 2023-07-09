# The Test Framework

## Syntax
The `core` module contains a framework for testing in checks in `src/test/java/de/firemage/autograder/core/framework`.
It allows you to create tests simply by writing valid Java code and inserting so-called *meta comments* that specify e.g. where the Autograder is expected to find problems.
Meta comments are enclosed in `/*@` and `@*/`, i.e. they are valid Java block comments with additional `@` characters.
The framework will parse them and remove the comments before passing the code to the Autograder, so that they do no impact the actual source code to check.
In particular, they can be also used within standard Java strings and comments.

The content of a meta comment consists of up to three parts, separated by semicolons.
Whitespace around the parts is ignored.
The first part must be a tag from the following table:

| tag            | description                                                                   |
|----------------|-------------------------------------------------------------------------------|
| <empty string> | No problem expected here; use for comments                                    |
| ok             | No problem expected here; used for documenting why this is the case           |
| not ok         | Expect a problem here                                                         |
| false positive | Expect a problem here, but warn about the existence of a false positive       |
| false negative | Don't expect a problem here, but warn about the existence of a false positive |

The second part of the meta comment is optional and can be used to provide a comment.
The third part of the meta comment is also optional and may only be present if the second (comment) part is present (which may be empty).
It should contain the name of the problem type to expect here.
If specified, a problem of this type is expected at this line, as long as the tag indicated that a problem is expected here.

## Examples
```
    /*@ not ok @*/
    
    /*@not ok@*/
    
    /*@ false positive; see github issue @*/
    
    /*@ ok; This is a comment*/
    
    /*@ ;This is only a comment @*/
```
