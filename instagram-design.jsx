import { useState } from "react";

const COLORS = {
  bg: "#0a0a0f",
  surface: "#12121a",
  border: "#1e1e2e",
  accent: "#e1306c",
  purple: "#833ab4",
  blue: "#405de6",
  gold: "#f5a623",
  green: "#2ecc71",
  cyan: "#00d2ff",
  text: "#e8e8f0",
  muted: "#6b6b8a",
};

const Box = ({ x, y, w, h, color, label, sublabel, icon, onClick, active }) => (
  <g onClick={onClick} style={{ cursor: onClick ? "pointer" : "default" }}>
    <rect
      x={x} y={y} width={w} height={h} rx={8}
      fill={active ? color + "33" : "#12121a"}
      stroke={active ? color : "#2a2a3e"}
      strokeWidth={active ? 2 : 1}
      style={{ transition: "all 0.2s" }}
    />
    {icon && <text x={x + w / 2} y={y + h / 2 - 10} textAnchor="middle" fontSize={18}>{icon}</text>}
    <text x={x + w / 2} y={y + h / 2 + (icon ? 8 : 4)} textAnchor="middle" fill={active ? color : COLORS.text} fontSize={11} fontWeight={600} fontFamily="monospace">{label}</text>
    {sublabel && <text x={x + w / 2} y={y + h / 2 + (icon ? 22 : 18)} textAnchor="middle" fill={COLORS.muted} fontSize={9} fontFamily="monospace">{sublabel}</text>}
  </g>
);

const Arrow = ({ x1, y1, x2, y2, color = "#2a2a3e", dashed, label }) => {
  const mx = (x1 + x2) / 2;
  const my = (y1 + y2) / 2;
  return (
    <g>
      <defs>
        <marker id={`arrow-${color.replace("#", "")}`} markerWidth="6" markerHeight="6" refX="5" refY="3" orient="auto">
          <path d="M0,0 L0,6 L6,3 z" fill={color} />
        </marker>
      </defs>
      <line
        x1={x1} y1={y1} x2={x2} y2={y2}
        stroke={color} strokeWidth={1.5}
        strokeDasharray={dashed ? "5,4" : "none"}
        markerEnd={`url(#arrow-${color.replace("#", "")})`}
        opacity={0.7}
      />
      {label && <text x={mx} y={my - 5} textAnchor="middle" fill={color} fontSize={8} fontFamily="monospace" opacity={0.9}>{label}</text>}
    </g>
  );
};

const SectionLabel = ({ x, y, text, color }) => (
  <text x={x} y={y} fill={color} fontSize={10} fontWeight={700} fontFamily="monospace" opacity={0.6} letterSpacing={2}>{text}</text>
);

const TABS = ["Architecture", "Feed Generation", "Data Flow", "Scale"];

const panels = {
  Architecture: () => (
    <svg viewBox="0 0 900 600" width="100%" style={{ maxHeight: 540 }}>
      {/* Background zones */}
      <rect x={10} y={10} width={880} height={580} rx={12} fill="#0d0d15" stroke="#1a1a2e" strokeWidth={1} />

      {/* Client Layer */}
      <rect x={20} y={20} width={160} height={560} rx={8} fill="#e1306c08" stroke="#e1306c22" strokeWidth={1} />
      <SectionLabel x={50} y={44} text="CLIENTS" color={COLORS.accent} />
      <Box x={32} y={52} w={136} h={44} color={COLORS.accent} label="iOS App" icon="📱" />
      <Box x={32} y={106} w={136} h={44} color={COLORS.accent} label="Android App" icon="🤖" />
      <Box x={32} y={160} w={136} h={44} color={COLORS.accent} label="Web Browser" icon="🌐" />

      {/* API Gateway */}
      <rect x={196} y={20} width={130} height={560} rx={8} fill="#f5a62308" stroke="#f5a62322" strokeWidth={1} />
      <SectionLabel x={210} y={44} text="GATEWAY" color={COLORS.gold} />
      <Box x={208} y={52} w={106} h={60} color={COLORS.gold} label="API Gateway" sublabel="Rate limit · Auth" icon="🔀" />
      <Box x={208} y={124} w={106} h={44} color={COLORS.gold} label="Load Balancer" sublabel="Round-robin" icon="⚖️" />
      <Box x={208} y={180} w={106} h={44} color={COLORS.gold} label="CDN" sublabel="CloudFront" icon="🌍" />
      <Box x={208} y={236} w={106} h={44} color={COLORS.gold} label="Auth Service" sublabel="JWT · OAuth" icon="🔐" />

      {/* Microservices */}
      <rect x={342} y={20} width={240} height={560} rx={8} fill="#405de608" stroke="#405de622" strokeWidth={1} />
      <SectionLabel x={388} y={44} text="MICROSERVICES" color={COLORS.blue} />
      <Box x={354} y={52} w={104} h={50} color={COLORS.blue} label="User Service" sublabel="profiles · follows" icon="👤" />
      <Box x={466} y={52} w={104} h={50} color={COLORS.blue} label="Post Service" sublabel="create · metadata" icon="📸" />
      <Box x={354} y={114} w={104} h={50} color={COLORS.cyan} label="Feed Service" sublabel="rank · assemble" icon="📰" />
      <Box x={466} y={114} w={104} h={50} color={COLORS.cyan} label="Fan-out Service" sublabel="push notifications" icon="📤" />
      <Box x={354} y={176} w={104} h={50} color={COLORS.purple} label="Media Service" sublabel="encode · compress" icon="🖼️" />
      <Box x={466} y={176} w={104} h={50} color={COLORS.purple} label="Search Service" sublabel="Elasticsearch" icon="🔍" />
      <Box x={354} y={238} w={104} h={50} color={COLORS.green} label="Notification Svc" sublabel="push · email" icon="🔔" />
      <Box x={466} y={238} w={104} h={50} color={COLORS.green} label="Story Service" sublabel="24h expiry" icon="⭕" />
      <Box x={354} y={300} w={216} h={50} color={COLORS.gold} label="Ranking / ML Service" sublabel="interest · recency · relationship scoring" icon="🤖" />
      <Box x={354} y={362} w={216} h={50} color={COLORS.accent} label="Message Queue (Kafka)" sublabel="async event streaming" icon="📨" />

      {/* Storage */}
      <rect x={598} y={20} width={290} height={560} rx={8} fill="#2ecc7108" stroke="#2ecc7122" strokeWidth={1} />
      <SectionLabel x={680} y={44} text="STORAGE" color={COLORS.green} />
      <Box x={610} y={52} w={128} h={50} color={COLORS.blue} label="PostgreSQL" sublabel="users · follows" icon="🗃️" />
      <Box x={748} y={52} w={128} h={50} color={COLORS.blue} label="Cassandra" sublabel="posts · likes" icon="🗄️" />
      <Box x={610} y={114} w={128} h={50} color={COLORS.accent} label="Redis Cluster" sublabel="feed cache · sessions" icon="⚡" />
      <Box x={748} y={114} w={128} h={50} color={COLORS.accent} label="Redis PubSub" sublabel="live notifications" icon="📡" />
      <Box x={610} y={176} w={128} h={50} color={COLORS.gold} label="S3 / Blob Store" sublabel="photos · videos" icon="🪣" />
      <Box x={748} y={176} w={128} h={50} color={COLORS.gold} label="Elasticsearch" sublabel="hashtags · users" icon="🔎" />
      <Box x={610} y={238} w={128} h={50} color={COLORS.green} label="InfluxDB" sublabel="metrics · analytics" icon="📊" />
      <Box x={748} y={238} w={128} h={50} color={COLORS.green} label="Zookeeper" sublabel="service registry" icon="🐘" />
      <Box x={610} y={300} w={266} h={50} color={COLORS.purple} label="Object CDN (CloudFront)" sublabel="global edge delivery · cache media" icon="🌐" />
      <Box x={610} y={362} w={266} h={50} color={COLORS.muted} label="Data Warehouse (Redshift)" sublabel="analytics · ML training data" icon="🏛️" />

      {/* Arrows - clients to gateway */}
      <Arrow x1={168} y1={74} x2={208} y2={74} color={COLORS.accent} label="HTTPS" />
      <Arrow x1={168} y1={128} x2={208} y2={100} color={COLORS.accent} />
      <Arrow x1={168} y1={182} x2={208} y2={126} color={COLORS.accent} />

      {/* gateway to services */}
      <Arrow x1={314} y1={80} x2={354} y2={77} color={COLORS.gold} />
      <Arrow x1={314} y1={100} x2={354} y2={140} color={COLORS.gold} />
      <Arrow x1={314} y1={140} x2={466} y2={77} color={COLORS.gold} />
      <Arrow x1={314} y1={190} x2={354} y2={202} color={COLORS.gold} />

      {/* services to storage */}
      <Arrow x1={570} y1={77} x2={610} y2={77} color={COLORS.blue} />
      <Arrow x1={570} y1={140} x2={610} y2={140} color={COLORS.cyan} />
      <Arrow x1={570} y1={202} x2={610} y2={202} color={COLORS.purple} />
      <Arrow x1={570} y1={265} x2={748} y2={77} color={COLORS.green} dashed />
      <Arrow x1={570} y1={320} x2={610} y2={325} color={COLORS.gold} dashed />

      {/* Kafka to services */}
      <Arrow x1={462} y1={387} x2={466} y2={265} color={COLORS.accent} dashed label="events" />
    </svg>
  ),

  "Feed Generation": () => (
    <svg viewBox="0 0 900 600" width="100%" style={{ maxHeight: 540 }}>
      <rect x={10} y={10} width={880} height={580} rx={12} fill="#0d0d15" stroke="#1a1a2e" strokeWidth={1} />

      {/* Title */}
      <text x={450} y={42} textAnchor="middle" fill={COLORS.cyan} fontSize={14} fontWeight={700} fontFamily="monospace" letterSpacing={3}>FEED GENERATION — HYBRID FANOUT MODEL</text>

      {/* POST creation flow */}
      <SectionLabel x={30} y={75} text="① POST CREATION (WRITE PATH)" color={COLORS.gold} />
      <Box x={20} y={84} w={90} h={44} color={COLORS.accent} label="User A Posts" icon="👤" />
      <Arrow x1={110} y1={106} x2={145} y2={106} color={COLORS.accent} label="upload" />
      <Box x={145} y={84} w={90} h={44} color={COLORS.gold} label="Post Service" icon="📸" />
      <Arrow x1={235} y1={106} x2={270} y2={106} color={COLORS.gold} />
      <Box x={270} y={84} w={90} h={44} color={COLORS.gold} label="S3 + CDN" icon="🪣" sublabel="media store" />
      <Arrow x1={360} y1={106} x2={395} y2={106} color={COLORS.gold} />
      <Box x={395} y={84} w={110} h={44} color={COLORS.purple} label="Fan-out Service" icon="📤" sublabel="checks follower count" />
      <Arrow x1={505} y1={90} x2={580} y2={70} color={COLORS.green} label="≤10K followers" />
      <Arrow x1={505} y1={122} x2={580} y2={142} color={COLORS.accent} label=">10K (celebrity)" />

      {/* Regular fanout */}
      <Box x={580} y={52} w={120} h={44} color={COLORS.green} label="Push to Redis" sublabel="all follower feeds" icon="⚡" />
      <Arrow x1={700} y1={74} x2={760} y2={74} color={COLORS.green} />
      <Box x={760} y={52} w={120} h={44} color={COLORS.green} label="Redis Feed List" sublabel="[postId, postId, ...]" icon="📋" />

      {/* Celebrity path */}
      <Box x={580} y={124} w={120} h={44} color={COLORS.accent} label="Cassandra" sublabel="store only, no push" icon="🗄️" />

      {/* READ path */}
      <SectionLabel x={30} y={210} text="② FEED READ (READ PATH)" color={COLORS.cyan} />
      <Box x={20} y={220} w={90} h={44} color={COLORS.blue} label="User Opens App" icon="📱" />
      <Arrow x1={110} y1={242} x2={145} y2={242} color={COLORS.blue} />
      <Box x={145} y={220} w={100} h={44} color={COLORS.blue} label="Feed Service" icon="📰" />

      <Arrow x1={245} y1={232} x2={290} y2={210} color={COLORS.green} label="fetch pre-built" />
      <Arrow x1={245} y1={252} x2={290} y2={272} color={COLORS.accent} label="live fetch" />

      <Box x={290} y={192} w={120} h={44} color={COLORS.green} label="Redis Cache" sublabel="regular follows" icon="⚡" />
      <Box x={290} y={252} w={120} h={44} color={COLORS.accent} label="Cassandra" sublabel="celebrity posts" icon="🗄️" />

      <Arrow x1={410} y1={214} x2={450} y2={240} color={COLORS.green} />
      <Arrow x1={410} y1={274} x2={450} y2={256} color={COLORS.accent} />

      <Box x={450} y={220} w={110} h={44} color={COLORS.purple} label="Merge + Rank" sublabel="ML scoring model" icon="🤖" />
      <Arrow x1={560} y1={242} x2={600} y2={242} color={COLORS.purple} />
      <Box x={600} y={220} w={110} h={44} color={COLORS.cyan} label="Top N Posts" sublabel="returned to client" icon="✅" />

      {/* RANKING box */}
      <SectionLabel x={30} y={340} text="③ RANKING SIGNALS" color={COLORS.purple} />
      {[
        ["🤝", "Relationship", "DMs, tags, mutual follows", 20],
        ["⭐", "Interest", "past likes/views on similar", 175],
        ["🕐", "Recency", "newer = higher base score", 330],
        ["📈", "Frequency", "how often you open app", 485],
        ["👥", "Following", "fewer follows = higher per-post weight", 640],
        ["🌊", "Usage", "short session = best posts only", 760],
      ].map(([icon, label, sub, x], i) => (
        <Box key={i} x={x} y={350} w={i < 5 ? 140 : 110} h={54} color={COLORS.purple} label={label} sublabel={sub} icon={icon} />
      ))}

      {/* Arrows from signals to rank */}
      <Arrow x1={90} y1={350} x2={450} y2={264} color={COLORS.purple} dashed />
      <Arrow x1={505} y1={350} x2={505} y2={264} color={COLORS.purple} dashed />

      {/* Celebrity problem note */}
      <rect x={20} y={430} width={860} height={140} rx={8} fill="#e1306c08" stroke="#e1306c22" />
      <text x={40} y={454} fill={COLORS.accent} fontSize={11} fontWeight={700} fontFamily="monospace">⚠  THE CELEBRITY PROBLEM — Why Pure Fan-out on Write Fails</text>
      <text x={40} y={475} fill={COLORS.muted} fontSize={10} fontFamily="monospace">Kylie Jenner: 400M followers. 1 post → 400M Redis writes simultaneously → thundering herd → Redis OOM</text>
      <text x={40} y={495} fill={COLORS.text} fontSize={10} fontFamily="monospace">Solution: threshold at ~10K followers. Celebrities never pre-push. Their posts are fetched live at read time and</text>
      <text x={40} y={511} fill={COLORS.text} fontSize={10} fontFamily="monospace">merged into the pre-built feed before ranking. Cost: 1 extra Cassandra query per celebrity you follow.</text>
      <text x={40} y={532} fill={COLORS.green} fontSize={10} fontFamily="monospace">Result: feed assembly in &lt;200ms even at 500M DAU. Redis write amplification capped at ~10K per post.</text>
      <rect x={668} y={440} width={200} height={118} rx={6} fill="#2ecc7111" stroke="#2ecc7133" />
      <text x={768} y={464} textAnchor="middle" fill={COLORS.green} fontSize={10} fontWeight={700} fontFamily="monospace">Feed Assembly Time</text>
      <text x={768} y={490} textAnchor="middle" fill={COLORS.green} fontSize={28} fontWeight={700} fontFamily="monospace">&lt;200ms</text>
      <text x={768} y={512} textAnchor="middle" fill={COLORS.muted} fontSize={9} fontFamily="monospace">p99 latency target</text>
      <text x={768} y={530} textAnchor="middle" fill={COLORS.muted} fontSize={9} fontFamily="monospace">500M DAU</text>
    </svg>
  ),

  "Data Flow": () => (
    <svg viewBox="0 0 900 600" width="100%" style={{ maxHeight: 540 }}>
      <rect x={10} y={10} width={880} height={580} rx={12} fill="#0d0d15" stroke="#1a1a2e" strokeWidth={1} />
      <text x={450} y={42} textAnchor="middle" fill={COLORS.cyan} fontSize={14} fontWeight={700} fontFamily="monospace" letterSpacing={3}>DATA FLOWS — WRITE vs READ PATHS</text>

      {/* WRITE PATH */}
      <SectionLabel x={30} y={72} text="WRITE PATH — Photo Upload" color={COLORS.gold} />
      {[
        { label: "Client", sub: "iOS/Android", x: 20, icon: "📱", color: COLORS.accent },
        { label: "API Gateway", sub: "auth · rate limit", x: 145, icon: "🔀", color: COLORS.gold },
        { label: "Post Service", sub: "validates · saves", x: 270, icon: "📸", color: COLORS.blue },
        { label: "S3 Bucket", sub: "raw photo stored", x: 395, icon: "🪣", color: COLORS.gold },
        { label: "Media Worker", sub: "resize · encode", x: 520, icon: "⚙️", color: COLORS.purple },
        { label: "CDN Push", sub: "edge cached", x: 645, icon: "🌍", color: COLORS.green },
        { label: "Kafka Event", sub: "post.created", x: 770, icon: "📨", color: COLORS.accent },
      ].map((n, i) => (
        <Box key={i} x={n.x} y={82} w={110} h={50} color={n.color} label={n.label} sublabel={n.sub} icon={n.icon} />
      ))}
      {[145, 270, 395, 520, 645, 770].map((x, i) => (
        <Arrow key={i} x1={x} y1={107} x2={x + 5} y2={107} color={COLORS.gold} />
      ))}
      {/* fixed arrows for write path */}
      <Arrow x1={130} y1={107} x2={145} y2={107} color={COLORS.gold} />
      <Arrow x1={255} y1={107} x2={270} y2={107} color={COLORS.gold} />
      <Arrow x1={380} y1={107} x2={395} y2={107} color={COLORS.gold} />
      <Arrow x1={505} y1={107} x2={520} y2={107} color={COLORS.gold} />
      <Arrow x1={630} y1={107} x2={645} y2={107} color={COLORS.gold} />
      <Arrow x1={755} y1={107} x2={770} y2={107} color={COLORS.gold} />

      {/* Kafka consumers */}
      <SectionLabel x={30} y={162} text="KAFKA CONSUMERS (async)" color={COLORS.accent} />
      <Box x={770} y={170} w={110} h={44} color={COLORS.accent} label="Kafka" sublabel="post.created" icon="📨" />
      <Arrow x1={770} y1={192} x2={645} y2={192} color={COLORS.accent} dashed label="fan-out worker" />
      <Arrow x1={770} y1={192} x2={520} y2={215} color={COLORS.accent} dashed label="notification worker" />
      <Arrow x1={770} y1={192} x2={395} y2={215} color={COLORS.accent} dashed label="search indexer" />
      <Arrow x1={770} y1={192} x2={270} y2={192} color={COLORS.accent} dashed label="analytics sink" />
      <Box x={520} y={170} w={110} h={44} color={COLORS.cyan} label="Fan-out Svc" sublabel="push to feeds" icon="📤" />
      <Box x={395} y={204} w={110} h={44} color={COLORS.green} label="Notif. Worker" sublabel="push alerts" icon="🔔" />
      <Box x={270} y={170} w={110} h={44} color={COLORS.purple} label="Search Indexer" sublabel="Elasticsearch" icon="🔍" />
      <Box x={145} y={170} w={110} h={44} color={COLORS.muted} label="Analytics" sublabel="Redshift / Spark" icon="📊" />

      {/* READ PATH */}
      <SectionLabel x={30} y={280} text="READ PATH — Feed Load" color={COLORS.cyan} />
      {[
        { label: "Client", sub: "opens app", x: 20, icon: "📱", color: COLORS.accent },
        { label: "API Gateway", sub: "JWT validate", x: 145, icon: "🔀", color: COLORS.gold },
        { label: "Feed Service", sub: "orchestrates", x: 270, icon: "📰", color: COLORS.cyan },
        { label: "Redis Cache", sub: "pre-built feed", x: 395, icon: "⚡", color: COLORS.green },
        { label: "Cassandra", sub: "celebrity posts", x: 520, icon: "🗄️", color: COLORS.blue },
        { label: "ML Ranker", sub: "score posts", x: 645, icon: "🤖", color: COLORS.purple },
        { label: "Response", sub: "top 20 posts", x: 770, icon: "✅", color: COLORS.green },
      ].map((n, i) => (
        <Box key={i} x={n.x} y={290} w={110} h={50} color={n.color} label={n.label} sublabel={n.sub} icon={n.icon} />
      ))}
      <Arrow x1={130} y1={315} x2={145} y2={315} color={COLORS.cyan} />
      <Arrow x1={255} y1={315} x2={270} y2={315} color={COLORS.cyan} />
      <Arrow x1={380} y1={315} x2={395} y2={315} color={COLORS.cyan} label="1. fetch cache" />
      <Arrow x1={505} y1={315} x2={520} y2={315} color={COLORS.cyan} label="2. celeb posts" />
      <Arrow x1={630} y1={315} x2={645} y2={315} color={COLORS.cyan} label="3. rank" />
      <Arrow x1={755} y1={315} x2={770} y2={315} color={COLORS.cyan} />

      {/* Database Schema Highlights */}
      <SectionLabel x={30} y={378} text="KEY DATA SCHEMAS" color={COLORS.purple} />

      {/* Users */}
      <rect x={20} y={388} width={195} height={180} rx={6} fill="#405de611" stroke="#405de633" />
      <text x={117} y={408} textAnchor="middle" fill={COLORS.blue} fontSize={10} fontWeight={700} fontFamily="monospace">users (PostgreSQL)</text>
      {["user_id UUID PK", "username VARCHAR", "email VARCHAR", "created_at TIMESTAMP", "follower_count INT", "is_celebrity BOOL"].map((f, i) => (
        <text key={i} x={32} y={424 + i * 16} fill={i === 0 ? COLORS.gold : COLORS.muted} fontSize={9} fontFamily="monospace">{f}</text>
      ))}

      {/* Posts */}
      <rect x={225} y={388} width={195} height={180} rx={6} fill="#833ab411" stroke="#833ab433" />
      <text x={322} y={408} textAnchor="middle" fill={COLORS.purple} fontSize={10} fontWeight={700} fontFamily="monospace">posts (Cassandra)</text>
      {["post_id TIMEUUID PK", "user_id UUID", "media_url TEXT", "caption TEXT", "like_count COUNTER", "created_at TIMESTAMP"].map((f, i) => (
        <text key={i} x={237} y={424 + i * 16} fill={i === 0 ? COLORS.gold : COLORS.muted} fontSize={9} fontFamily="monospace">{f}</text>
      ))}

      {/* Feed */}
      <rect x={430} y={388} width={195} height={180} rx={6} fill="#e1306c11" stroke="#e1306c33" />
      <text x={527} y={408} textAnchor="middle" fill={COLORS.accent} fontSize={10} fontWeight={700} fontFamily="monospace">feed_cache (Redis)</text>
      {['Key: "feed:{user_id}"', "Type: Sorted Set", "Member: post_id", "Score: timestamp", "Max size: 1000 items", "TTL: 24 hours"].map((f, i) => (
        <text key={i} x={442} y={424 + i * 16} fill={i < 2 ? COLORS.gold : COLORS.muted} fontSize={9} fontFamily="monospace">{f}</text>
      ))}

      {/* Follows */}
      <rect x={635} y={388} width={245} height={180} rx={6} fill="#2ecc7111" stroke="#2ecc7133" />
      <text x={757} y={408} textAnchor="middle" fill={COLORS.green} fontSize={10} fontWeight={700} fontFamily="monospace">follows (PostgreSQL)</text>
      {["follower_id UUID FK", "following_id UUID FK", "created_at TIMESTAMP", "PRIMARY KEY(follower,following)", "INDEX on following_id", "→ sharded by follower_id"].map((f, i) => (
        <text key={i} x={647} y={424 + i * 16} fill={i < 2 ? COLORS.gold : COLORS.muted} fontSize={9} fontFamily="monospace">{f}</text>
      ))}
    </svg>
  ),

  Scale: () => (
    <svg viewBox="0 0 900 600" width="100%" style={{ maxHeight: 540 }}>
      <rect x={10} y={10} width={880} height={580} rx={12} fill="#0d0d15" stroke="#1a1a2e" strokeWidth={1} />
      <text x={450} y={42} textAnchor="middle" fill={COLORS.cyan} fontSize={14} fontWeight={700} fontFamily="monospace" letterSpacing={3}>SCALE NUMBERS & BOTTLENECKS</text>

      {/* Metrics */}
      {[
        { label: "Daily Active Users", val: "500M", color: COLORS.accent, x: 30, y: 70 },
        { label: "Photos Uploaded / Day", val: "100M", color: COLORS.gold, x: 230, y: 70 },
        { label: "Feed Reads / Second", val: "~4M", color: COLORS.cyan, x: 430, y: 70 },
        { label: "Feed Latency p99", val: "<200ms", color: COLORS.green, x: 630, y: 70 },
      ].map((m, i) => (
        <g key={i}>
          <rect x={m.x} y={m.y} width={180} height={72} rx={8} fill={m.color + "15"} stroke={m.color + "44"} />
          <text x={m.x + 90} y={m.y + 28} textAnchor="middle" fill={m.color} fontSize={9} fontFamily="monospace" letterSpacing={1}>{m.label.toUpperCase()}</text>
          <text x={m.x + 90} y={m.y + 56} textAnchor="middle" fill={m.color} fontSize={30} fontWeight={800} fontFamily="monospace">{m.val}</text>
        </g>
      ))}

      {/* Bottlenecks */}
      <SectionLabel x={30} y={172} text="BOTTLENECKS & SOLUTIONS" color={COLORS.accent} />

      {[
        {
          prob: "Media Upload Latency",
          sol: "Pre-signed S3 URLs — client uploads directly to S3. Your servers never touch the bytes. Bypasses your infra entirely.",
          icon: "🖼️", color: COLORS.gold, x: 20, y: 182,
        },
        {
          prob: "Feed Read Latency",
          sol: "Redis Sorted Sets pre-computed on write. Cap at 1000 post IDs per user. Trim oldest on insert. O(log N) reads.",
          icon: "⚡", color: COLORS.cyan, x: 20, y: 254,
        },
        {
          prob: "Celebrity Fan-out",
          sol: "Threshold at 10K followers. Celebrities are flagged in user table. Fan-out service skips Redis push; stores only in Cassandra.",
          icon: "⭐", color: COLORS.accent, x: 20, y: 326,
        },
        {
          prob: "Hot Cassandra Partitions",
          sol: "Shard key: (user_id + time_bucket). Avoids single partition absorbing all writes for popular users. Bucket by day/week.",
          icon: "🗄️", color: COLORS.purple, x: 20, y: 398,
        },
        {
          prob: "CDN Cache Miss on New Posts",
          sol: "Proactive CDN push on upload completion. Don't wait for first user request to cache. Use CloudFront's API to pre-warm edges.",
          icon: "🌍", color: COLORS.green, x: 20, y: 470,
        },
      ].map((b, i) => (
        <g key={i}>
          <rect x={b.x} y={b.y} width={860} height={64} rx={6} fill={b.color + "0a"} stroke={b.color + "33"} />
          <text x={b.x + 18} y={b.y + 22} fill={b.color} fontSize={14} fontFamily="monospace">{b.icon}</text>
          <text x={b.x + 44} y={b.y + 23} fill={b.color} fontSize={11} fontWeight={700} fontFamily="monospace">PROBLEM: {b.prob}</text>
          <text x={b.x + 44} y={b.y + 42} fill={COLORS.muted} fontSize={10} fontFamily="monospace">→  {b.sol}</text>
        </g>
      ))}

      {/* Sharding strategy */}
      <rect x={20} y={546} width={860} height={34} rx={6} fill="#00d2ff08" stroke="#00d2ff22" />
      <text x={40} y={566} fill={COLORS.cyan} fontSize={10} fontWeight={700} fontFamily="monospace">REPLICATION:  </text>
      <text x={160} y={566} fill={COLORS.muted} fontSize={10} fontFamily="monospace">PostgreSQL primary + 2 read replicas per region  ·  Cassandra RF=3, quorum reads  ·  Redis Cluster 6 nodes (3 primary + 3 replica)  ·  Multi-AZ across 3 zones</text>
    </svg>
  ),
};

export default function InstagramDesign() {
  const [active, setActive] = useState("Architecture");

  return (
    <div style={{
      background: COLORS.bg,
      minHeight: "100vh",
      fontFamily: "monospace",
      padding: "16px",
      color: COLORS.text,
    }}>
      {/* Header */}
      <div style={{ textAlign: "center", marginBottom: 20 }}>
        <div style={{ fontSize: 28, marginBottom: 4 }}>📸</div>
        <div style={{
          fontSize: 20, fontWeight: 800, letterSpacing: 4,
          background: `linear-gradient(135deg, ${COLORS.accent}, ${COLORS.purple}, ${COLORS.blue})`,
          WebkitBackgroundClip: "text", WebkitTextFillColor: "transparent",
        }}>INSTAGRAM SYSTEM DESIGN</div>
        <div style={{ color: COLORS.muted, fontSize: 11, letterSpacing: 2, marginTop: 4 }}>500M DAU · 100M uploads/day · &lt;200ms feed</div>
      </div>

      {/* Tabs */}
      <div style={{ display: "flex", gap: 8, marginBottom: 16, flexWrap: "wrap", justifyContent: "center" }}>
        {TABS.map(tab => (
          <button
            key={tab}
            onClick={() => setActive(tab)}
            style={{
              padding: "8px 18px",
              borderRadius: 6,
              border: `1px solid ${active === tab ? COLORS.accent : COLORS.border}`,
              background: active === tab ? COLORS.accent + "22" : COLORS.surface,
              color: active === tab ? COLORS.accent : COLORS.muted,
              cursor: "pointer",
              fontSize: 11,
              fontWeight: 700,
              fontFamily: "monospace",
              letterSpacing: 1,
              transition: "all 0.15s",
            }}
          >{tab.toUpperCase()}</button>
        ))}
      </div>

      {/* Diagram */}
      <div style={{
        background: COLORS.surface,
        borderRadius: 12,
        border: `1px solid ${COLORS.border}`,
        padding: 8,
        overflowX: "auto",
      }}>
        {panels[active]?.()}
      </div>

      {/* Legend */}
      <div style={{ display: "flex", gap: 20, marginTop: 16, flexWrap: "wrap", justifyContent: "center" }}>
        {[
          ["Client Layer", COLORS.accent],
          ["Gateway", COLORS.gold],
          ["Microservices", COLORS.blue],
          ["Cache", COLORS.cyan],
          ["Storage", COLORS.green],
          ["ML/Ranking", COLORS.purple],
        ].map(([label, color]) => (
          <div key={label} style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 10, color: COLORS.muted }}>
            <div style={{ width: 10, height: 10, borderRadius: 2, background: color + "66", border: `1px solid ${color}` }} />
            {label}
          </div>
        ))}
      </div>
    </div>
  );
}
