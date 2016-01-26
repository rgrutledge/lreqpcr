# The LRE Analyzer #
## Absolute quantification made simple ##

NOTE THAT DUE TO GOOGLE DISABLING FILE DOWNLOADING (JAN14), THE MOST CURRENT VERSION OF THE LRE ANALYZER AND RELATED FILES MUST BE RETRIEVED FROM THE LRE WEBSITE:
https://sites.google.com/site/lreqpcr/program-files

The LRE Analyzer was developed as a standalone, platform independent desktop program that automates real-time qPCR data analysis using a method called "Linear Regression of Efficiency" or LRE qPCR.

Originating from modeling PCR amplification as a sigmoidal process, target quantity is derived directly from the fluorescence readings within the central region of an amplification profile.

As such, the LRE Analyzer allows absolute quantification to be conducted without the need for standard curves, in addition to providing a number of quality control capabilities not possible using conventional qPCR methods based on Cq/Ct/Cp.

These includes the ability to determine amplification efficiency within individual amplification reactions, in addition to identification of aberrant amplification kinetics.

The [LRE qPCR](http://sites.google.com/site/lreqpcr) website provides additional information, including a three part video describing how LRE was conceived, along with a detailed overview of methods used to evaluate the accuracy and dynamic range of LRE qPCR.

In addition to a quick start document, demonstration database files are provided within the program download, which help to illustrate how the program functions. The program also has extensive help that provides a detailed overview of how to use the program, along with guidelines on how to implement LRE-based data analysis.

Note that installing and running the program does not require administrative privileges.

Note also that Java Runtime Environment 1.6 is required. If problems are encountered [java.com](http://java.com) provides a diagnostic tool.

Version 0.9: Along with improvements in data display and storage, this latest version of the LRE Analyzer implements nonlinear regression analysis that improves both the robustness and accuracy of LRE qPCR.

An article describing an early version the LRE Analyzer is available:
[Rutledge RG (2011) A Java program for LRE-based real-time qPCR that enables large-scale absolute quantification. PLoS ONE 6(3): e17636](http://www.plosone.org/article/info%3Adoi%2F10.1371%2Fjournal.pone.0017636).

The LRE Analyzer was written using the NetBeans IDE, and utilizes the modular architecture and windowing system provided by the NetBeans Platform.