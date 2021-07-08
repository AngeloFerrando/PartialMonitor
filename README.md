# PartialMonitor

Very small Java program to transform LamaConv monitors into partial monitors.

## How to install

- Install LamaConv (https://www.isp.uni-luebeck.de/lamaconv)

## How to use

Compile the Java code.

```bash
-$ javac Monitor.java
```

Execute the program

```bash
-$ java Monitor <path_to_lamaconv> <LTL_property> <alphabet>
```

where <path_to_lamaconv> is the path to the folder where LamaConv has been installed (where rltlconv.jar is), <LTL_property> is the LTL property to use for synthesising the monitor, and <alphabet> is the list of alphabet of the system under analysis.

For instance

```bash
-$ java Monitor "((p && Fq) || (s && GFr))" [s,p,q,r]
```

will generate a monitor with the additional (x) state representing when the monitor should give up on verifying the property.
