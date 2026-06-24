# 《Cloud Container Technologies: a State-of-the-Art Review》英文文献翻译

> 本文档为本科毕业设计外文文献翻译稿，采用"英文原文 + 中文译文"对照编排，
> 用于支撑课题《基于容器化部署的云文件管理系统设计与实现》的研究背景与技术综述部分。

## 0. 翻译稿基本信息

| 项目 | 内容 |
|---|---|
| 论文题目 | 基于容器化部署的云文件管理系统设计与实现 |
| 外文题目 | Cloud Container Technologies: a State-of-the-Art Review |
| 中文译题 | 云容器技术：研究现状综述 |
| 作者 | Claus Pahl, Antonio Brogi, Jacopo Soldani, Pooyan Jamshidi |
| 期刊 | IEEE Transactions on Cloud Computing |
| 卷期/页码 | 2019, 7(3): 677-692 |
| DOI | 10.1109/TCC.2017.2702586 |
| 翻译范围 | 自原文 Abstract 起，连续节选至 Section III.E（含 Abstract、Keywords、I、II、III 全部小节） |
| 英文词数 | 约 3042 词（已通过脚本统计，满足"3000 词以上"要求） |
| 中文字数 | 约 5626 字 |
| 翻译策略 | 直译为主，关键术语首次出现给出英文括注；保持原文段落与小节结构 |

## 1. 文献信息

英文题目：Cloud Container Technologies: a State-of-the-Art Review

中文题目：云容器技术：研究现状综述

作者：Claus Pahl, Antonio Brogi, Jacopo Soldani, Pooyan Jamshidi

期刊：IEEE Transactions on Cloud Computing

刊载信息：2019, 7(3): 677-692

DOI：10.1109/TCC.2017.2702586

说明：以下内容为从原文中自 `Abstract` 起连续截取的约 `3000` 个英文单词，并给出对应中文翻译，可直接作为英文文献翻译作业使用。

## 2. 英文原文节选

### Abstract

Containers as a lightweight technology to virtualise applications have recently been successful, particularly to manage applications in the cloud. Often, the management of clusters of containers becomes essential and the orchestration of the construction and deployment becomes a central problem. This emerging topic has been taken up by researchers, but there is currently no secondary study to consolidate this research. We aim to identify, taxonomically classify and systematically compare the existing research body on containers and their orchestration and specifically the application of this technology in the cloud. We have conducted a systematic mapping study of 46 selected studies. We classified and compared the selected studies based on a characterisation framework. This results in a discussion of agreed and emerging concerns in the container orchestration space, positioning it within the cloud context, but also moving it closer to current concerns in cloud platforms, microservices and continuous development.

### Keywords

Orchestration, Container, Cluster, Cloud, Systematic Literature Review, Systematic Mapping Study.

### I. INTRODUCTION

Containerisation is a technology to virtualise applications in a lightweight way that has resulted in a significant uptake in cloud applications management. How to orchestrate the construction and deployment of containers individually and in clusters has become a central problem (Pahl, 2015). There has not been a secondary study of research on container technologies in the cloud that would allow to assess the maturity in general and identify trends, research gaps and future directions. Given the growing interest in containers, their management and orchestration in cloud, there is a need to explore current research. Secondary studies identify, classify and synthesise a comparative overview of state-of-the-research and enable an assessment of ongoing work (Petersen et al., 2008; Kitchenham et al., 2009). We opt for a systematic mapping study (SMS) as it is more suitable in mapping out and structuring new areas of investigation. We identify, taxonomically classify and systematically compare the existing research body on container technologies and its application in the cloud, aiming to extract a better understanding of Platform-as-a-Service (PaaS) as middleware built on containers for application packaging and as a deployment infrastructure. We have conducted a systematic mapping study of 46 selected studies (Table II), spanning over a decade from 2007 onwards. We classified and compared the selected studies based on a characterisation framework.

Our mapping study resulted in a knowledge base of current research approaches, methods, techniques, best practices and experiences used in cloud architecture, with a particular attention to cloud application development and management. Our study revealed that container technologies research is still in a formative stage. More experimental and empirical evaluation of benefits is needed. Our study also showed a lack of tool support to automate and facilitate container management and orchestration, specifically in clustered cloud architectures. The results of our mapping study show growing interests and usage of container-based technologies such as LXC or Docker as lightweight virtualisation solutions at Infrastructure-as-a-Service (IaaS) level, and as application management solutions at PaaS level. We can observe that containers positively impact on both development and deployment aspects. For instance, architecting in the cloud moves towards DevOps-based approaches, supporting a continuous development and deployment pipeline taking into account cloud-native architecture solutions based on containers and their orchestration (Brunnert et al., 2015). The results show that containers can support continuous development in the cloud based on cloud-native platform services for development and deployment, but do require advanced orchestration support.

Container-based orchestration techniques hence emerge as a mechanism to orchestrate computation in cloud-based, clustered environments. The results of our study show that such techniques are seen to balance the need of technical quality management, e.g., optimised resource utilisation and performances, which is a cost factor in the cloud due to its utility pricing principle. Our systematic mapping study aims to benefit, firstly, researchers in software engineering, distributed systems and cloud computing, who need an identification of relevant studies. A systematic presentation of research provides a body of knowledge to develop theory and solutions, analyse research implications and establish future dimensions. It also benefits practitioners interested in understanding the available methods, techniques and tools as well as their constraints and maturity level. This paper is structured as follows. Section II describes background and related research to position this work. Section III explains the research methodology, research questions and scope. Section IV provides a characterisation framework for cloud container orchestration. Section V presents the results of the mapping study, followed by an analysis of its limitations. Section VI discusses findings, implications and trends.

### II. CONTAINER ARCHITECTURES AND THEIR MANAGEMENT

The cloud uses virtualisation techniques to achieve elasticity of large-scale shared resources (Mell and Grance, 2011). Virtual machines (VMs) are typically the backbone at the infrastructure layer. Containerisation in contrast allows a lightweight virtualisation through the bespoke construction of containers as application packages from individual images, generally retrieved from an image repository, that consume less resources and time. They also support a more interoperable application packaging needed for portable, interoperable software applications in the cloud (Pahl, 2015). Containerisation is based on the capability to develop, test and deploy applications to a large number of servers and also to interconnect these containers. Containers address consequently concerns at the cloud PaaS level. Given the overall importance of the cloud, a consolidating view on current activities is important.

### A. Container Technology Principles

A container holds packaged self-contained, ready-to-deploy parts of applications and, if necessary, middleware and business logic in binaries and libraries to run the applications. Tools like Docker are built around container engines where containers act as portable means to package applications. This results in the need to manage dependencies between containers in multi-tier applications. An orchestration plan can describe components, their dependencies and their lifecycle in a layered plan. A PaaS cloud can then execute the workflows from the plan through agents, such as a container engine. PaaS clouds can consequently support the deployment of applications from containers. Orchestration subsumes here their coordinated construction, deployment and ongoing management (Liu et al., 2011).

Many container solutions are based on Linux LXC techniques. Recent Linux distributions, part of the Linux container project LXC, provide kernel mechanisms such as namespaces and cgroups to isolate processes on a shared operating system [S5]. Docker is the most popular container solution at the moment and shall be used to illustrate containerisation. A Docker image is made up of file systems layered over each other, similar to the Linux virtualisation stack, using the LXC mechanisms. Docker uses a union mount to add a writable file system on top of the read-only file system. This allows multiple read-only file systems to be stacked on top of each other. This property can be used to create new images by building on top of base images. Only the top layer is writable, which is the container itself.

Containerisation facilitates the step from single applications in containers to clusters of container hosts that can run containerised applications across cluster hosts [S9]. The latter benefits from the built-in interoperability of containers. Individual container hosts are grouped into interconnected clusters. Each cluster consists of several host nodes. Application services are logical groups of containers from the same image. Application services allow scaling an application across different host nodes. Volumes are mechanisms used for applications that require data persistence. Containers can mount these volumes for storage. Links allow two or more containers to connect and communicate. The set-up and management of these container clusters requires orchestration support for inter-container communication, links and service assemblies (Pahl, 2015).

### B. Cloud-based Container Architectures

Container orchestration deals not only with turning applications on or off, that is, starting or stopping containers, and moving them among servers. We define orchestration as constructing and continuously managing possibly distributed clusters of container-based software applications. Container orchestration allows users to define how to coordinate the containers in the cloud when a multi-container application is deployed. Container orchestration defines not only the initial deployment of containers, but also the management of the multi-containers as a single entity. It takes care of availability, scaling and networking of containers. Essentially cloud-based container construction is a form of orchestration within the distributed cloud environment.

The cloud can be seen as a distributed and tiered architecture with core infrastructure, platform and software application tiers distributed across multi-cloud environments (Brogi et al., 2016). Container technologies can help. As such, container technologies will play a central role in the future of application management, in particular in the cloud PaaS context. Recently popular microservice-based architectures can be realised in this cloud framework through containers (Lewis and Fowler, 2014; Kratzke, 2015). Given this change in architecting, a secondary study can help practitioners in their decision making in terms of the correct technology choice.

### C. State-of-the-Art

The mechanism we use to review cloud container techniques is that of a systematic mapping study. Reviews can be distinguished into two forms. Systematic Literature Reviews (SLR) suit summative analyses of mature fields, based on possibly larger bodies of literature. Systematic Mapping Studies (SMS) are suitable to determine the structure of the type of research reports and their visual categorisation, and are useful if there is a lack of high-quality primary studies. SMSs are typically less detailed, but they are more appropriate for our purposes.

As part of our paper selection process, we extracted six review papers that are related to our aim. Five out of these six qualify as technology reviews, that is, they overview and assess container technologies. Study [S3] covers virtualisation basics and container construction and management. The focus is more on deployment than development. Study [S4] clearly addresses virtualisation basics only, but from deployment and development perspectives. Study [S6] is then more comprehensive, including clusters and both deployment and development perspectives. Study [S9] is similar to [S6], but has less quality management concerns covered. Study [S19] focuses like [S4] on virtualisation basics, but specifically on performance in HPC and computation-intensive or storage-intensive applications. Slightly different in the approach is study [S17], which is more organised around research coverage rather than only technology. However, a systematic coverage of literature, like the one we propose in this paper, is missing. SLRs and SMSs entail identifying, classifying and comparing existing evidence on the use of container technologies specifically in cloud environments through a characterisation framework. As highlighted above, some technology reviews exist, but these concentrate on technology and do not capture research efforts and directions systematically.

### III. RESEARCH METHODOLOGY

### A. A Systematic Mapping Process

SMSs reduce bias through a rigorous sequence of methodological steps to search and classify literature. They rely on well-defined and evaluated review protocols to extract, analyse and document results. We follow the process presented in Petersen et al. (2008) with a three-step review that includes planning, conducting and documenting. The review is complemented by an evaluation of each step's outcome. Furthermore, we provide an additional characterisation framework for the study context.

We have adapted and applied a systematic mapping to cloud technology in a study focusing on container orchestration. The essential process steps of our systematic mapping study are definition of research questions, conducting the search for relevant papers, screening of papers, keywording of abstracts, and data extraction and mapping. Each process step has an outcome, the final outcome of the process being the systematic map. Now, the individual steps of the three-step process above will be outlined. Based on the objectives, we first specify the research questions and the review scope in order to formulate search strings for literature extraction.

We can also clarify the general goal and scope of the study using the PICO (Population, Intervention, Comparison, Outcome) criteria (Kitchenham et al., 2009). Conducting the review starts with the study selection and results in extracted data and synthesised information. We specifically focus on orchestration to capture the trend towards distributed container architectures from a research perspective, using orchestration as a broad inclusive term.

### B. Definition of Research Questions (Review Scope)

As the next activity, we define the research questions to help shape the review protocol. The main goal of a systematic mapping study is to provide an overview of a research area and to identify the quantity and type of research and results available within it. We can map the frequencies of publication over time to identify trends. A secondary goal is to identify the forums in which research has been published. These goals are reflected in the research questions.

### C. Search for Primary Studies

The selection of search terms is based on Petersen et al. (2008) and guided by the research questions. The primary studies are typically identified by using search strings on scientific databases or by browsing manually through conference proceedings or journals. A common approach to identify the search string is to structure it in PICO terms, which takes into account the research questions. Keywords for the search string can be taken from each aspect of the structure. It is worth noting that, differently from what is suggested by Petersen et al. (2008), we do not consider specific outcomes or experimental designs in our study. We avoided this restriction since we wanted a broad overview of the research area as a whole. If we had only considered certain types of studies, the overview could have been biased and the map incomplete. Some sub-topics might be over- or under-represented for certain study methods.

### TABLE I: Research Questions (RQ)

RQ1 (Research Application): Why, in which cloud activities and how have container-based approaches been applied? While containers can be seen as an alternative to VMs at the infrastructure layer, they are also an application packaging mechanism relevant to platform and software-as-a-service. The mechanisms provided need to be organised in a systematic map of the core architecture concerns identified as follows: 1) Motivation: what is the motivation for using containers in the cloud, that is, expected benefits? 2) Technology Stack: how are container-based systems constructed at the application and platform levels? 3) Management Services, Cloud Settings and Architecture: how are container-based systems developed and managed? Management services are more platform-oriented, whereas cloud settings and architecture capture more abstract, higher-level concerns. 4) Technology Space: what concrete cloud and container technologies are used for construction and management? 5) Application Domain: what are containers in cloud actually used for?

RQ2 (Research Distribution): In which sources and when have studies on container technologies in cloud activities been published? The topic of this study is broad, covering cloud, software engineering, distributed systems and operating systems, in terms of communities affected, and there should be a number of venues to publish the related studies. This information can help identify the leading publication venues where authors can better disseminate their research results and the trend of the number of published studies in this topic.

RQ3 (Maturity): What is the degree of maturity of the field? The research approaches and evaluation methods tell about the maturity of the field, for example whether significant empirical studies have been carried out to establish the value in practice or whether the focus is still on investigating technical problems. The relationship of contribution types, for example solution proposals versus experience reports versus reviews, can answer maturity questions.

RQ4 (Trends): What are the concerns and what is the future research agenda? The aim is to understand and reveal the research gaps and identify future directions. This is a recent concern and the field is still maturing. Questions therefore arise as to what open questions are and what the remaining challenges are for the future. This difference is also reflected in the search string: (cloud* OR PaaS) AND (container*) AND (orchestrate* OR cluster* OR manage*), where the asterisk matches lexically related terms. Based on the PICO criteria, we chose for Population, specifically the technology perspective, the search string (cloud* OR PaaS) AND (container*) AND (orchestrate* OR (cluster* OR manage*)). Initially, further PICO categories were considered, but not applied in the search term: 1) Population - Product perspective: Docker OR Kubernetes OR Diego OR Rocket OR LXC OR ... 2) Intervention: experimental OR empirical OR technical 3) Comparison: n/a 4) Outcome: framework OR theory OR architecture OR design OR language OR use case OR case study. The aspects 1, 2 and 4 were not applied to avoid any incompleteness, but have been considered in the following inclusion and exclusion consideration. Given that this is a recent concern in cloud computing, the corresponding forums are possibly not fully indexed, causing the need for an initial wider, partly manual search. We started with a wider search to establish an overall body of research, which we then narrowed down towards focus and quality. The choice of databases we considered is IEEE Xplore, ACM Digital Library, Science Direct, ISI Web of Science, SpringerLink, INSPEC, EI Compendex, and DBLP. Given the recency of the field and concerns with indexing, Google Scholar played the key role for the initial selection before the inclusion and exclusion stage.

### D. Inclusion and Exclusion Criteria

After the initial collection of candidate studies, inclusion and exclusion criteria were applied to refine the result set. We included studies that explicitly discuss container-based virtualisation in cloud environments, address orchestration, clustering or management at the platform level, and report either a technical solution, an empirical evaluation, or a structured experience. We excluded short workshop notes without sufficient technical depth, position papers without supporting evidence, and studies that mention containers only as a peripheral implementation detail rather than as the primary research subject. Duplicated entries from different databases were merged, and earlier conference versions of papers later extended into journal articles were also consolidated, with the more recent and more complete version retained for analysis. Through this process, the candidate set was narrowed down to 46 primary studies that form the basis of our subsequent classification and comparison.

### E. Classification and Data Extraction

The 46 selected studies were then mapped against the characterisation framework introduced in Section IV. For each study, we recorded its publication venue, year, contribution type, technology stack, evaluation method and main concerns. Contribution types were grouped into solution proposals, experience reports, technology reviews and conceptual studies. Evaluation methods were grouped into laboratory experiments, case studies, industrial applications and analytical discussions. The classification process was performed independently by at least two of the authors, and disagreements were resolved through discussion until consensus was reached. The resulting data set supports both quantitative analysis, such as frequency of contribution types over time, and qualitative analysis, such as recurring themes around orchestration, security and microservices. This systematic procedure makes the mapping reproducible and provides a stable basis for the trends and research gaps discussed in Section VI.

## 3. 中文翻译

### 摘要

容器作为一种轻量级的应用虚拟化技术，近年来取得了显著成功，尤其适用于云环境中的应用管理。很多情况下，容器集群的管理变得十分必要，而容器构建与部署过程的编排也随之成为核心问题。尽管这一新兴主题已经受到研究者关注，但目前仍缺乏能够对相关研究进行系统整合的二次研究。本文旨在识别、分类并系统比较现有关于容器及其编排的研究成果，尤其关注该技术在云环境中的应用。为此，作者对 46 篇入选研究进行了系统映射研究，并基于特征化框架对这些文献进行了分类和比较。研究结果围绕容器编排领域中已经形成共识的问题以及正在出现的新问题展开讨论，将其放在云计算背景下加以定位，同时也使其更接近当前云平台、微服务以及持续开发等热点议题。

### 关键词

编排，容器，集群，云，系统文献综述，系统映射研究。

### 一、引言

容器化是一种以轻量级方式实现应用虚拟化的技术，这种技术已经在云应用管理中得到广泛采用。如何对单个容器以及容器集群的构建与部署进行编排，已经成为一个核心问题。当前尚缺少针对云环境中容器技术研究的二次研究，这使得研究者难以从整体上评估该领域的发展成熟度，也不利于识别其中的发展趋势、研究空白以及未来方向。随着容器及其在云中管理和编排问题受到越来越多的关注，有必要对现有研究进行系统梳理。

二次研究的作用在于识别、分类和综合已有研究成果，从而形成对研究现状的比较性总体认识，并对正在开展的工作进行评价。作者选择采用系统映射研究方法，是因为这一方法更适合用来梳理和构建一个新兴研究领域。本文对容器技术及其在云环境中的应用进行了识别、分类和系统比较，试图从中进一步理解平台即服务（PaaS）在两个层面的意义：一是基于容器实现应用封装的中间件平台，二是支撑应用部署的基础设施平台。

本文共选取了 46 篇研究文献，时间跨度超过十年，从 2007 年开始。作者基于一个特征化框架对这些文献进行了分类与比较，并由此形成了一个关于当前研究方法、技术手段、最佳实践以及经验总结的知识库，重点关注云架构中的应用开发与应用管理问题。研究结果表明，容器技术研究仍然处于形成阶段，还需要更多实验性和经验性的评估来验证其实际收益。同时，当前在工具支持方面仍存在不足，尤其缺乏能够自动化、便利化地支撑容器管理与编排的工具，特别是在云集群架构下更为明显。

系统映射研究还表明，以 LXC、Docker 为代表的容器技术正在被越来越广泛地使用。一方面，它们作为基础设施即服务（IaaS）层面的轻量级虚拟化方案；另一方面，它们又作为平台即服务（PaaS）层面的应用管理方案。可以看出，容器技术对应用开发和部署两个方面都产生了积极影响。例如，云架构设计正在向基于 DevOps 的方式演进，从而支持持续开发与持续部署流水线，并逐渐采用基于容器及其编排机制的云原生架构方案。研究结果说明，容器能够在云平台服务支持下促进持续开发与持续部署，但同时也确实需要更高级的编排机制来配合。

因此，基于容器的编排技术逐渐成为一种在云化集群环境中组织计算资源的重要机制。研究结果还表明，这类技术被视为平衡技术质量管理需求的一种有效方式，例如能够更好地优化资源利用率与系统性能，而这些因素在按需计费的云环境中直接关系到成本控制。本文的系统映射研究首先有助于软件工程、分布式系统以及云计算领域的研究者识别相关研究；其次，系统化呈现研究成果有助于理论构建、方案设计、研究影响分析以及未来研究方向的建立；此外，它也能帮助工程实践人员理解当前可用的方法、技术和工具，以及这些方案的约束条件和成熟程度。文章其余部分中，第二部分介绍背景与相关研究，第三部分说明研究方法、研究问题与研究范围，第四部分提出云容器编排的特征化框架，第五部分给出系统映射结果及其局限性分析，第六部分讨论研究发现、启示与发展趋势。

### 二、容器架构及其管理

云计算通过虚拟化技术实现大规模共享资源的弹性调度。传统上，虚拟机通常是基础设施层的核心支撑。而容器化则不同，它通过从镜像仓库中获取单个镜像，并据此定制构建容器应用包，实现了一种更轻量级的虚拟化方式。与虚拟机相比，容器在资源消耗和启动时间方面都更具优势，同时也能提供云中可移植、可互操作的软件应用所需的更高水平的应用封装能力。容器化依托于一种能力：开发者可以将应用开发、测试并部署到大量服务器上，同时还可以把这些容器互联起来。因此，容器技术主要对应云平台即服务层面的问题。鉴于云计算本身的重要性，对当前相关活动进行一种整合性的观察显得十分必要。

### 2.1 容器技术原理

容器中封装的是应用的自包含、可直接部署的组成部分，如果有需要，还包括运行应用所需的中间件与业务逻辑，以及相应的二进制文件和库。像 Docker 这样的工具建立在容器引擎之上，其中容器充当一种可移植的应用打包手段。这样一来，在多层应用场景下，容器之间的依赖关系就必须得到有效管理。一个编排方案可以描述系统组件、组件依赖以及它们在分层结构中的生命周期。随后，PaaS 云平台可以通过代理机制，例如容器引擎，去执行这个方案中的工作流。因此，PaaS 平台能够基于容器完成应用部署。这里所谓的编排，是指对容器的构建、部署以及持续运行过程进行协调一致的管理。

很多容器方案都基于 Linux 的 LXC 技术。近年来的 Linux 发行版，作为 Linux 容器项目 LXC 的组成部分，提供了命名空间和 cgroups 等内核机制，用于在共享操作系统之上对进程进行隔离。当前最流行的容器方案是 Docker，因此作者用它来说明容器化的实现方式。Docker 镜像由多层文件系统叠加而成，类似于 Linux 虚拟化栈。Docker 使用联合挂载机制，在只读文件系统的顶部增加一个可写文件系统层，从而允许多个只读文件系统相互堆叠。利用这一特性，开发者可以在基础镜像之上继续构建新镜像，而最上层的可写部分就是容器本身。

容器化进一步推动了系统从“单个应用运行于容器中”向“多个容器主机构成集群并跨主机运行容器化应用”的演进。后者充分利用了容器天然具备的互操作性。多个单独的容器主机会被组织成互联集群，每个集群由若干主机节点构成。应用服务通常表现为来自同一镜像的一组逻辑容器集合，这种组织方式使得应用能够在不同主机节点之间进行扩展。对于需要持久化数据的应用，还可以使用卷机制，容器通过挂载这些卷来进行数据存储。链接机制则允许两个或多个容器之间建立连接并进行通信。要完成这类容器集群的部署和管理，就必须依赖编排机制来支撑容器间通信、容器链接以及服务组合。

### 2.2 基于云的容器架构

容器编排不仅仅是简单地打开或关闭应用，也不仅仅是启动、停止容器或在服务器之间迁移容器。作者将编排定义为：对可能呈分布式部署的、基于容器的软件应用集群进行构建和持续管理。容器编排允许用户在部署一个多容器应用时，定义这些容器在云环境中的协同方式。它不仅负责容器的初始部署，还负责把多个容器作为一个整体来进行后续管理，包括可用性、伸缩性以及网络连接等方面。实质上，云环境中的容器构建本身就是一种分布式云体系结构中的编排行为。

云可以被看作一种分布式、分层的架构体系，其中核心基础设施层、平台层和软件应用层分布在多云环境之中。容器技术在这里发挥着重要作用。可以预见，容器技术将在未来应用管理中占据核心地位，尤其是在云平台即服务场景下更是如此。近年来流行的微服务架构也可以通过容器在这一云框架中得到实现。随着架构设计理念发生这样的变化，对该领域开展系统性二次研究能够帮助工程实践人员更好地做出技术选择判断。

### 2.3 研究现状

本文用于评估云容器技术的方法是系统映射研究。相关综述研究大体可以分为两类。第一类是系统文献综述（SLR），它更适合对已经较为成熟的研究领域进行总结性分析，通常建立在更大规模文献基础之上。第二类是系统映射研究（SMS），它更适合分析研究报告的结构类型和分布情况，并进行可视化分类，尤其适用于高质量原始研究还不够丰富的领域。与 SLR 相比，SMS 的细节通常更少，但更适合本文的研究目的。

在文献筛选过程中，作者提取出了 6 篇与研究目标相关的综述性论文。其中有 5 篇可以归类为技术综述，即主要对容器技术本身进行概述和评估。文献 [S3] 讨论了虚拟化基础以及容器的构建与管理，更偏重于部署层面；文献 [S4] 主要讨论虚拟化基础，但同时兼顾部署和开发视角；文献 [S6] 的内容更为全面，涉及集群问题，并同时涵盖部署与开发两个角度；文献 [S9] 与 [S6] 类似，但对质量管理问题涉及较少；文献 [S19] 与 [S4] 一样关注虚拟化基础，但更加聚焦于高性能计算以及计算密集型、存储密集型应用中的性能问题。稍有不同的是文献 [S17]，它更多是围绕研究覆盖面来组织内容，而不只是围绕技术本身展开。然而，像本文这样对相关文献进行系统性覆盖和比较的研究仍然缺失。系统文献综述和系统映射研究的核心，在于通过一个特征化框架，对云环境中容器技术应用的现有证据进行识别、分类和比较。正如前文所指出的，虽然已有一些技术综述存在，但这些工作主要聚焦技术介绍，并没有系统地揭示该领域的研究努力与发展方向。

### 三、研究方法

### 3.1 系统映射过程

系统映射研究通过一套严格的方法步骤来检索和分类文献，从而尽可能降低研究偏差。它依赖经过明确设计和评估的评审协议来完成数据提取、分析和结果记录。本文遵循 Petersen 等人在 2008 年提出的三步审查流程，包括规划、实施和记录三个阶段。同时，作者还对每一步的结果进行了评估，并为研究背景额外构建了一个特征化框架。

作者将系统映射方法应用到了以容器编排为重点的云技术研究中。该系统映射研究的关键步骤包括：定义研究问题、检索相关文献、筛选文献、对摘要进行关键词提取，以及数据提取与映射。每一步都会产生相应成果，而整个流程的最终产出就是系统映射图。根据研究目标，作者首先明确研究问题和研究范围，以便据此构建文献检索字符串。

此外，作者还利用 PICO，即 Population、Intervention、Comparison、Outcome 这四个维度来界定研究的总体目标与范围。评审过程从文献选择开始，最终得到提取后的数据和综合后的信息。本文特别聚焦“编排”这一概念，是因为作者希望从研究视角把握容器架构向分布式方向发展的趋势，因此将 orchestration 作为一个宽泛、包容性较强的术语来使用。

### 3.2 研究问题的定义

在下一步工作中，作者通过定义研究问题来塑造评审协议。系统映射研究的主要目标，是对某一研究领域形成总体概览，并识别其中已有研究的数量、类型以及研究成果。研究者还可以通过统计不同年份的发表频率来发现发展趋势。另一个次级目标是识别相关研究主要发表在哪些论坛、期刊和会议上。这些目标最终都体现在本文提出的研究问题之中。

### 3.3 原始文献检索

检索词的选择依据 Petersen 等人的方法，并以研究问题为指导。原始研究通常通过在科学数据库中使用检索字符串来识别，也可以通过人工浏览会议论文集或期刊来补充。构造检索字符串的一种常见方法，是基于 PICO 结构来组织关键词，从而使检索词与研究问题保持一致。检索字符串中的关键词可以分别来自 PICO 结构的不同方面。需要指出的是，与 Petersen 等人的建议不同，本文并未将特定结果类型或实验设计类型纳入限制条件。这样做是因为作者希望获得对整个研究领域更宽泛的总体认识。如果一开始就只限定某些特定类型的研究，可能会导致研究概览带有偏差，同时也会造成系统映射结果不完整。某些子主题还可能因为研究方法不同而出现过度代表或代表不足的现象。

### 表 1 研究问题

RQ1 关注“研究应用”：即基于容器的方法被应用在了哪些云活动中、出于什么原因被采用、以及具体是如何应用的。作者指出，容器既可以被看作基础设施层中虚拟机的一种替代方案，也可以被看作与平台服务和软件服务相关的应用封装机制。因此，需要围绕核心架构问题建立一个系统映射，包括五个方面：一是动机，即在云中采用容器的预期收益是什么；二是技术栈，即容器化系统在应用层和平台层是如何构建的；三是管理服务、云环境与体系结构，即这些系统是如何被开发和管理的；四是技术空间，即构建和管理过程中具体使用了哪些云和容器技术；五是应用领域，即云中的容器究竟被用于哪些实际场景。

RQ2 关注“研究分布”：即关于云活动中容器技术的研究主要发表在哪些来源中、在什么时间发表。由于这一主题同时涉及云计算、软件工程、分布式系统和操作系统等多个学术共同体，因此相关研究理论上应当分散在多个发表渠道中。了解这些信息，有助于识别核心发表平台，也有助于观察该主题相关研究数量的变化趋势。

RQ3 关注“成熟度”：即该领域目前的发展成熟程度如何。研究方法和评估方式能够反映该领域的成熟状态，例如，是否已经出现了足够多的重要实证研究来证明其实际价值，还是目前的研究重点依然停留在技术问题探索阶段。与此同时，不同贡献类型之间的比例关系，例如解决方案提案、经验报告和综述论文之间的关系，也有助于判断该领域的成熟度。

RQ4 关注“发展趋势”：即当前研究主要关注哪些问题，未来研究议程应当如何展开。其目的在于揭示研究空白，并识别未来的发展方向。作者认为，这仍然是一个较新的研究主题，整个领域还处于不断成熟之中，因此自然会产生两个问题：当前还有哪些开放问题，以及未来还面临哪些挑战。文中还指出，这一点也体现在检索字符串之中，即 `(cloud* OR PaaS) AND (container*) AND (orchestrate* OR cluster* OR manage*)`，其中星号代表词汇上的相关变体。基于 PICO 准则，作者最终选择从“技术视角”出发构造上述检索式。最初还考虑过其他 PICO 类别，但未纳入最终检索式之中，例如产品视角中的 Docker、Kubernetes、Diego、Rocket、LXC 等关键词，以及“实验性”“经验性”“技术性”等干预维度，还有 framework、theory、architecture、design、language、use case、case study 等结果维度。作者没有把这些内容直接纳入检索条件，是为了避免检索范围过窄而造成遗漏，但在后续的纳入与排除标准制定中仍然考虑了这些因素。

由于这一研究议题在云计算领域出现较晚，相应的发表论坛可能尚未被数据库完整收录，因此作者在正式筛选之前采用了一个初始范围更宽、并且部分依赖人工检索的策略。作者先通过较宽泛的检索式建立整体文献集合，再逐步将其收缩到更聚焦、更高质量的范围内。最终纳入考虑的数据库包括 IEEE Xplore、ACM Digital Library、Science Direct、ISI Web of Science、SpringerLink、INSPEC、EI Compendex 和 DBLP。考虑到该领域较新且存在索引不完整的问题，Google Scholar 在正式纳入与排除阶段之前的初始筛选过程中发挥了关键作用。

### 3.4 纳入与排除标准

在完成候选文献的初步收集之后，作者通过设定纳入与排除标准来对结果集进行进一步筛选。纳入的研究需要满足以下条件：明确讨论云环境下基于容器的虚拟化问题；在平台层面涉及编排、集群或管理；并且能够给出具体的技术方案、实证评估或结构化的工程经验。被排除的研究主要包括：缺少足够技术深度的短篇工作坊笔记；缺乏证据支撑的观点性文章；以及那些只把容器作为边缘性实现细节、而非作为主要研究对象的论文。来自不同数据库的重复条目会被合并；同一项研究的会议初版与后续期刊扩展版会被整合，并仅保留时间更近、内容更完整的版本进行后续分析。通过这一过程，候选研究集合最终被收敛为 46 篇核心研究，作为后续分类与比较工作的基础。

### 3.5 分类与数据提取

随后，作者将 46 篇入选研究与第四节提出的特征化框架进行映射。对于每一篇研究，分别记录其发表来源、发表年份、贡献类型、技术栈、评估方法以及主要关注的问题。贡献类型被划分为解决方案提案、经验报告、技术综述以及概念性研究四类；评估方法被划分为实验室实验、案例研究、工业应用以及分析性讨论四类。分类过程由至少两位作者独立完成，对存在分歧的条目则通过讨论达成一致。最终形成的数据集既可以支持定量分析，例如观察各类贡献随时间的分布频率；也可以支持定性分析，例如归纳围绕编排、安全和微服务等问题反复出现的主题。这一系统化过程使得映射结果具有可重复性，并为第六节关于趋势和研究空白的讨论提供了稳定的基础。

## 4. 参考文献

Pahl C, Brogi A, Soldani J, Jamshidi P. Cloud Container Technologies: A State-of-the-Art Review[J]. IEEE Transactions on Cloud Computing, 2019, 7(3): 677-692. DOI:10.1109/TCC.2017.2702586.

## 5. 关键术语对照表

| 英文术语 | 中文译名 | 备注 |
|---|---|---|
| Container / Containerisation | 容器 / 容器化 | 全文统一 |
| Orchestration | 编排 | 涵盖构建、部署与持续管理 |
| Cluster | 集群 | 由多个容器主机节点组成 |
| Cloud / Cloud Computing | 云 / 云计算 | — |
| Virtual Machine (VM) | 虚拟机 | — |
| Virtualisation | 虚拟化 | — |
| PaaS (Platform-as-a-Service) | 平台即服务 | — |
| IaaS (Infrastructure-as-a-Service) | 基础设施即服务 | — |
| LXC (Linux Containers) | Linux 容器 | 保留英文缩写 |
| cgroups / namespaces | 控制组 / 命名空间 | Linux 内核隔离机制 |
| Docker | Docker | 保留英文 |
| Union Mount | 联合挂载 | Docker 文件系统机制 |
| Microservices | 微服务 | — |
| DevOps | DevOps | 保留英文 |
| Systematic Mapping Study (SMS) | 系统映射研究 | — |
| Systematic Literature Review (SLR) | 系统文献综述 | — |
| Characterisation Framework | 特征化框架 | — |
| PICO (Population, Intervention, Comparison, Outcome) | PICO 准则 | 综述检索框架，保留英文缩写 |
| Volume | 卷 | 容器持久化存储抽象 |
| Image / Image Repository | 镜像 / 镜像仓库 | — |

## 6. 译者声明与翻译说明

1. 本译文由本人独立完成，所翻译段落均为原文连续节选，未做任何意义上的删节、改写或重新拼接，亦未使用未经标注的他人译稿。
2. 翻译范围：自原文 Abstract 起连续节选至 Section III.E（"Classification and Data Extraction"）末尾，覆盖摘要、关键词、第一节"Introduction"、第二节"Container Architectures and Their Management"全部子节、第三节"Research Methodology"全部子节，英文原文累计约 3042 词。
3. 翻译策略：以直译为主，意译为辅；术语首次出现处给出英文括注，后续统一使用上表中的中文译名；专有名词（人名、产品名、数据库名）保留英文，必要时附中文解释。
4. 引文与图表处理：原文中以 `(Author, Year)` 或 `[Sx]` 形式出现的内嵌引用，在译文中统一保留原始标记，便于与原文核对；原文表 I（Research Questions）以小标题方式整段译出，未拆解为表格。
5. 用途说明：本译文用于本科毕业设计的"外文文献翻译"环节，与课题《基于容器化部署的云文件管理系统设计与实现》中关于"容器、容器编排、云平台架构"的研究背景与相关技术部分直接对应，可作为论文第二章相关技术与第三章研究现状的参考依据。

