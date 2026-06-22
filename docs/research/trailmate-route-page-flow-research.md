# TrailMate 路线页动线调研

日期：2026-06-20

## 外部模式

- AllTrails 将路线发现、路线详情、导航、离线地图和偏航提醒组织成行前到途中闭环。对 TrailMate 的启发是：路线页需要先给出路线是否适合，再把地图导航作为现场工具，而不是把诊断和依据放在首屏。
- komoot 强调规划路线、离线地图和户外导航。对 TrailMate 的启发是：计划、导航和出发准备应分层，不要在一个滚动页里并列。
- Gaia GPS 的核心体验是地图、当前位置、离线地图和轨迹记录。对 TrailMate 的启发是：路线 Tab 的核心画布应是地图和当前位置，技术状态只在需要处理时出现。
- Strava Beacon 展示了记录中安全分享的模式。对 TrailMate 的启发是：安全分享属于现场导航动作，不属于设置页或个人页。

## TrailMate 设计结论

路线页应采用三态动线：

1. 无路线：引导导入 GPX，不展示评估、地图诊断或空的技术状态。
2. 行前准备：默认进入评估，回答“这条线适不适合我”，主动作指向检查装备或进入路线。
3. 现场导航：路线 Tab 以地图、当前位置、当前检查点、进度和一个主动作组成驾驶舱；诊断、图层和授权详情只在用户主动展开时出现。

## 页面禁区

- 不在首屏展示高德 key、SDK、隐私授权、点数量等工程诊断。
- 不把用户画像、历史 GPX、身高体重或 AI 输入作为可见证据堆叠。
- 不让评估页和路线页都显示同一张大地图，避免用户感到重复。
- 不把计划、装备、轨迹记录和诊断同时铺在一个首屏。

## 参考来源

- AllTrails: https://www.alltrails.com/welcome
- AllTrails Android GPS troubleshooting: https://support.alltrails.com/hc/en-us/articles/360019246391-Resolving-GPS-errors-on-Android
- komoot features: https://www.komoot.com/features
- Gaia GPS help overview: https://help.gaiagps.com/hc/en-us/articles/9067661557399-How-to-Use-Gaia-GPS
- Strava Beacon: https://support.strava.com/hc/en-us/articles/224357527-Strava-Beacon
