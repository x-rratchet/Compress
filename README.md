# `Compress` 

## 概述 

 - Android实现解压缩Rar/Zip文件
 - 支持Rar和zip，不支持7z 7z压缩算法比较复杂，压缩率高，需要使用c来压缩，使用java太耗时

## 如何使用

### Step 1. 添加 `JitPack` 仓库到你的 `build` 文件

在你的项目根目录下的 `build.gradle` 添加仓库：

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}	
```

### Step 2. 添加 `Compress` 的引用

```
dependencies {
	compile 'com.github.RRatChet:Compress:0.0.1'
}
```

## 使用方法

### 1 . 解压文件

```java
// 提取压缩文件.
CompressHelper.extract(zipFilePath, output);
```

### 2 . 压缩文件

```
// Rar方式压缩文件
CompressHelper.compressRarFile(filePath, outRarString);

// zip方式压缩文件
CompressHelper.compressZipFile(targetFileList, targetZipFile);
```






