const { useState, useRef, useEffect, createContext, useContext } = React;

// ─── Themes Configuration ────────────────────────────────────────────────────
const THEMES = {
  light: {
    name: "light",
    bg:        "#f8fafc", // Light clean background
    sidebar:   "#ffffff", // Crisp white sidebar
    panel:     "#ffffff", // Crisp white header/panels
    card:      "#f1f5f9", // Soft light slate for cards & bubbles
    hover:     "#e2e8f0", // Light border / hover lines
    accent:    "#6366f1", // Vibrant indigo accent
    accentDk:  "#ffffff", // Contrast text on accent elements
    accentLt:  "#e0e7ff", // Light indigo tint
    accentMid: "#4f46e5", // Deep indigo text / active states
    text1:     "#0f172a", // Dark slate primary text
    text2:     "#64748b", // Slate secondary text
    text3:     "#6366f1", // Accent text / initials
    danger:    "#ef4444", // Crisp red
    online:    "#22c55e", // Green online indicator
    offline:   "#94a3b8", // Gray offline indicator
    groupBg:   "#6366f1", // Group initials avatar background
    topNavBg:  "rgba(255, 255, 255, 0.92)",
    modalOverlay: "rgba(15, 23, 42, 0.5)",
    boxBorder: "1.5px solid #000000",
  },
  dark: {
    name: "dark",
    bg:        "#0b1326", // Deep dark background
    sidebar:   "#111a30", // Dark slate sidebar
    panel:     "#17223b", // Dark panel
    card:      "#1e2c4c", // Card background
    hover:     "#28375a", // Dark hover / border lines
    accent:    "#8083ff", // Neon indigo accent
    accentDk:  "#ffffff", // White on accent
    accentLt:  "rgba(128, 131, 255, 0.2)",
    accentMid: "#8083ff", // Light indigo text
    text1:     "#f8fafc", // Crisp light primary text
    text2:     "#94a3b8", // Muted light slate
    text3:     "#c3c0ff", // Accent light text
    danger:    "#ef4444", // Red danger
    online:    "#22c55e", // Green online
    offline:   "#64748b", // Gray offline
    groupBg:   "#4f46e5", // Group avatar bg
    topNavBg:  "rgba(17, 26, 48, 0.92)",
    modalOverlay: "rgba(0, 0, 0, 0.75)",
    boxBorder: "1.5px solid #3b4d75",
  }
};

const ThemeContext = createContext({
  theme: "light",
  setTheme: () => {},
  C: THEMES.light,
  lastSeenPrivacy: "Everyone",
  setLastSeenPrivacy: () => {},
  profilePhotoPrivacy: "Everyone",
  setProfilePhotoPrivacy: () => {},
  userProfile: {
    name: "You",
    username: "@you",
    bio: "Building things on the internet 🛠️",
    avatar: null,
  },
  setUserProfile: () => {},
});

const useTheme = () => useContext(ThemeContext);

// ─── Mock Data ────────────────────────────────────────────────────────────────
const CURRENT_USER = {
  id: "me",
  name: "You",
  username: "@you",
  email: "hello@example.com",
  avatar: null,
  initials: "YO",
  color: "#6366f1",
};

const USERS = [
  { id: "1", name: "Arun Kumar",      username: "@arunkumar",  email: "arun@example.com",   avatar: "https://i.pravatar.cc/150?img=11", online: true,  lastSeen: "Today at 10:14 AM", isContact: true,  bio: "Product Designer at StudioCraft. Coffee & typography lover ☕️" },
  { id: "2", name: "Priya Sharma",    username: "@priya",      email: "priya@example.com",  avatar: "https://i.pravatar.cc/150?img=5",  online: false, lastSeen: "Today at 09:47 AM", isContact: true,  bio: "UX researcher | Chai over coffee ☕" },
  { id: "3", name: "Maya Chen",       username: "@mayachen",   email: "maya@example.com",   avatar: "https://i.pravatar.cc/150?img=9",  online: false, lastSeen: "Yesterday at 6:30 PM", isContact: false, bio: "Photographer & visual storyteller 📷" },
  { id: "4", name: "Rahul Nair",      username: "@rahulnair",  email: "rahul@example.com",  avatar: "https://i.pravatar.cc/150?img=13", online: true,  lastSeen: "Today at 11:05 AM", isContact: true,  bio: "Full-stack dev, open-source enthusiast" },
  { id: "5", name: "Alex Morgan",     username: "@alexm",      email: "alex@example.com",   avatar: "https://i.pravatar.cc/150?img=17", online: false, lastSeen: "2 days ago", isContact: false, bio: "Design systems nerd" },
  { id: "6", name: "Sneha Patel",     username: "@sneha",      email: "sneha@example.com",  avatar: "https://i.pravatar.cc/150?img=21", online: true,  lastSeen: "Just now", isContact: true,  bio: "Founder @ BuildNow" },
];

const STICKERS = [
  { id: "s1", emoji: "🎉", label: "Party" },
  { id: "s2", emoji: "🔥", label: "Fire" },
  { id: "s3", emoji: "💜", label: "Love" },
  { id: "s4", emoji: "🚀", label: "LFG" },
  { id: "s5", emoji: "😂", label: "LOL" },
  { id: "s6", emoji: "👏", label: "Clap" },
  { id: "s7", emoji: "✨", label: "Sparkle" },
  { id: "s8", emoji: "🌟", label: "Star" },
  { id: "s9", emoji: "🎯", label: "Goals" },
];

const INITIAL_CHATS = [
  {
    id: "c1", type: "direct", userId: "1",
    messages: [
      { id: "m1", from: "1", type: "text", text: "Hey! Are you free tomorrow? Checking out that new studio space downtown.", time: "10:14 AM", status: "read", starred: true },
      { id: "m2", from: "me", type: "text", text: "Yes, what time? I can join after our 2 PM design review.", time: "10:18 AM", status: "read", replyTo: { name: "Arun Kumar", text: "Checking out that new studio space…" } },
      { id: "m3", from: "1", type: "image", url: "https://images.unsplash.com/photo-1497366216548-37526070297c?w=400", caption: "Found this corner spot with great natural light", time: "10:22 AM", status: "read" },
      { id: "m4", from: "1", type: "video", thumb: "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=400", duration: "0:42", time: "10:25 AM", status: "read" },
      { id: "m5", from: "me", type: "sticker", emoji: "🚀", label: "LFG!", time: "10:28 AM", status: "read" },
      { id: "m6", from: "me", type: "text", text: "Are you coming tomorrow?", time: "10:32 AM", status: "read", forwarded: true },
    ],
    lastMessage: "Are you coming tomorrow?", lastTime: "10:32 AM", unread: 0
  },
  {
    id: "c2", type: "direct", userId: "2",
    messages: [
      { id: "m1", from: "me", type: "text", text: "Okay 👍", time: "09:45 AM", status: "delivered" },
      { id: "m2", from: "2", type: "text", text: "Great! See you then 😊", time: "09:47 AM", status: "read" },
    ],
    lastMessage: "Okay 👍", lastTime: "09:45 AM", unread: 1
  },
  {
    id: "g1", type: "group", name: "College Friends", initials: "CF", color: "#6366f1",
    members: ["1","2","3","4"],
    messages: [
      { id: "m1", from: "4", type: "text", text: "See you there!", time: "Yesterday", status: "read" },
      { id: "m2", from: "3", type: "text", text: "Can't wait! 🎉", time: "Yesterday", status: "read" },
    ],
    lastMessage: "Rahul: See you there", lastTime: "Yesterday", unread: 4
  },
  {
    id: "c3", type: "direct", userId: "3",
    messages: [
      { id: "m1", from: "3", type: "image", url: "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400", caption: "Look at this view!", time: "Yesterday", status: "read" },
    ],
    lastMessage: "Sent an attachment", lastTime: "Yesterday", unread: 0
  },
  {
    id: "g2", type: "group", name: "Project Core", initials: "PC", color: "#6366f1",
    members: ["1","5","6"],
    messages: [
      { id: "m1", from: "5", type: "text", text: "Design looks solid!", time: "2d ago", status: "read" },
    ],
    lastMessage: "Alex: Design looks solid!", lastTime: "2d ago", unread: 0
  },
];

const INITIAL_STATUSES = [
  {
    id: "st1", userId: "1", type: "image",
    url: "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600",
    caption: "Views from the hike today 🏔️",
    time: "2h ago", viewed: false
  },
  {
    id: "st2", userId: "2", type: "text",
    text: "Shipping something exciting this week! 🚀",
    bg: "#8083ff", textColor: "#fff",
    time: "5h ago", viewed: true
  },
  {
    id: "st3", userId: "4", type: "text",
    text: "Coffee + code = productivity ☕",
    bg: "#3626ce", textColor: "#c3c0ff",
    time: "8h ago", viewed: false
  },
];

// ─── Helpers ──────────────────────────────────────────────────────────────────
const getUser = (id) => USERS.find(u => u.id === id);
const Avatar = ({ user, size = 40, showOnline = false }) => {
  const { C, lastSeenPrivacy, profilePhotoPrivacy, userProfile } = useTheme();
  const effectiveUser = user?.id === "me" ? {
    ...user,
    name: userProfile?.name || user?.name || "You",
    username: userProfile?.username || user?.username || "@you",
    avatar: userProfile?.avatar !== undefined ? userProfile?.avatar : user?.avatar,
    initials: (userProfile?.name || "You").trim().split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase() || "YO",
  } : user;

  const sz = `${size}px`;
  const dotSz = size <= 28 ? 8 : size <= 40 ? 12 : 16;
  const dotOff = size <= 28 ? -1 : 0;

  // Profile photo privacy visibility
  const canSeeAvatar = effectiveUser?.id === "me" 
    ? true 
    : profilePhotoPrivacy === "Everyone" || (profilePhotoPrivacy === "My Contacts" && effectiveUser?.isContact);

  // Online dot visibility based on Last Seen privacy
  const canSeeOnline = lastSeenPrivacy === "Everyone" || (lastSeenPrivacy === "My Contacts" && effectiveUser?.isContact);

  return (
    <div style={{ position: "relative", flexShrink: 0, width: sz, height: sz }}>
      {effectiveUser?.avatar && canSeeAvatar ? (
        <img src={effectiveUser.avatar} alt={effectiveUser.name}
          style={{ width: sz, height: sz, borderRadius: "50%", objectFit: "cover", display: "block" }} />
      ) : (
        <div style={{
          width: sz, height: sz, borderRadius: "50%",
          background: effectiveUser?.color || C.hover, display: "flex", alignItems: "center", justifyContent: "center",
          fontSize: size * 0.35, fontWeight: 700, color: C.text3, fontFamily: "'Plus Jakarta Sans', sans-serif"
        }}>
          {effectiveUser?.initials || effectiveUser?.name?.slice(0,2).toUpperCase() || "??"}
        </div>
      )}
      {showOnline && canSeeOnline && (
        <div style={{
          position: "absolute", bottom: dotOff, right: dotOff,
          width: dotSz, height: dotSz, borderRadius: "50%",
          background: effectiveUser?.online ? C.online : C.offline,
          border: `2px solid ${C.sidebar}`,
        }} />
      )}
    </div>
  );
};

const CheckIcon = ({ status, light }) => {
  const { C } = useTheme();
  const col = light ? "rgba(255,255,255,0.9)" : (status === "read" ? C.accentMid : C.text2);
  if (status === "sent") return <span style={{ color: col, fontSize: 11, fontWeight: 700 }}>✓</span>;
  if (status === "delivered") return <span style={{ color: col, fontSize: 11, fontWeight: 700 }}>✓✓</span>;
  if (status === "read") return <span style={{ color: light ? "#60a5fa" : C.accentMid, fontSize: 11, fontWeight: 700 }}>✓✓</span>;
  return null;
};

// ─── Icons (inline SVG) ───────────────────────────────────────────────────────
const Ic = {
  chat: () => <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="1.5" width="20" height="20"><path d="M2 10C2 5.58 5.58 2 10 2s8 3.58 8 8-3.58 8-8 8H2l2-2a7.95 7.95 0 01-2-6z" strokeLinejoin="round"/></svg>,
  group: () => <svg viewBox="0 0 22 16" fill="none" stroke="currentColor" strokeWidth="1.5" width="22" height="16"><circle cx="8" cy="6" r="4"/><path d="M1 15c0-3.31 3.13-6 7-6s7 2.69 7 6"/><circle cx="17" cy="5" r="3"/><path d="M21 15c0-2.76-1.79-5.1-4.27-5.76"/></svg>,
  status: () => <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="1.5" width="20" height="20"><circle cx="10" cy="10" r="8"/><circle cx="10" cy="10" r="3"/></svg>,
  profile: () => <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" strokeWidth="1.5" width="20" height="20"><circle cx="10" cy="6" r="4"/><path d="M2 18c0-4.42 3.58-8 8-8s8 3.58 8 8"/></svg>,
  settings: () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" width="20" height="20" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/></svg>,
  search: () => <svg viewBox="0 0 14 14" fill="none" stroke="currentColor" strokeWidth="1.5" width="14" height="14"><circle cx="6" cy="6" r="4"/><path d="M10 10l3 3" strokeLinecap="round"/></svg>,
  send: () => <svg viewBox="0 0 16 14" fill="none" stroke="currentColor" strokeWidth="1.5" width="16" height="14"><path d="M1 7L15 1l-4 12-3-5-7-1z" strokeLinejoin="round"/></svg>,
  attach: () => (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="20" height="20">
      <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" />
    </svg>
  ),
  emoji: () => <svg viewBox="0 0 17 17" fill="none" stroke="currentColor" strokeWidth="1.5" width="17" height="17"><circle cx="8.5" cy="8.5" r="7"/><path d="M5.5 10.5s.83 1 3 1 3-1 3-1" strokeLinecap="round"/><circle cx="6" cy="6.5" r="1" fill="currentColor" stroke="none"/><circle cx="11" cy="6.5" r="1" fill="currentColor" stroke="none"/></svg>,
  image: () => <svg viewBox="0 0 17 17" fill="none" stroke="currentColor" strokeWidth="1.5" width="17" height="17"><rect x="1" y="2" width="15" height="13" rx="2"/><circle cx="6" cy="7" r="1.5" fill="currentColor" stroke="none"/><path d="M1 13l4-4 3 3 3-3 5 4" strokeLinecap="round" strokeLinejoin="round"/></svg>,
  reply: () => (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="15" height="15">
      <polyline points="9 17 4 12 9 7" />
      <path d="M20 18v-2a4 4 0 0 0-4-4H4" />
    </svg>
  ),
  forward: () => (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="15" height="15">
      <polyline points="15 17 20 12 15 7" />
      <path d="M4 18v-2a4 4 0 0 1 4-4h12" />
    </svg>
  ),
  copy: () => (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="15" height="15">
      <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
      <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
    </svg>
  ),
  trash: () => (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="15" height="15">
      <polyline points="3 6 5 6 21 6" />
      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
      <line x1="10" y1="11" x2="10" y2="17" />
      <line x1="14" y1="11" x2="14" y2="17" />
    </svg>
  ),
  more: () => <svg viewBox="0 0 3 14" fill="currentColor" width="3" height="14"><circle cx="1.5" cy="2" r="1.5"/><circle cx="1.5" cy="7" r="1.5"/><circle cx="1.5" cy="12" r="1.5"/></svg>,
  close: () => <svg viewBox="0 0 10 10" fill="none" stroke="currentColor" strokeWidth="1.5" width="10" height="10"><path d="M1 1l8 8M9 1l-8 8" strokeLinecap="round"/></svg>,
  play: () => <svg viewBox="0 0 11 14" fill="currentColor" width="11" height="14"><path d="M1 1l10 6-10 6z"/></svg>,
  plus: () => <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" strokeWidth="1.5" width="16" height="16"><path d="M8 3v10M3 8h10" strokeLinecap="round"/></svg>,
  bell: () => <svg viewBox="0 0 20 21" fill="none" stroke="currentColor" strokeWidth="1.5" width="20" height="21"><path d="M10 2a7 7 0 017 7v4l2 2H1l2-2V9a7 7 0 017-7z"/><path d="M8 18a2 2 0 004 0"/></svg>,
  star: () => <svg viewBox="0 0 20 19" fill="none" stroke="currentColor" strokeWidth="1.5" width="20" height="19"><path d="M10 1l2.39 7.26H19l-5.55 4.03 2.12 6.52L10 14.77l-5.57 4.04 2.12-6.52L1 8.26h6.61z" strokeLinejoin="round"/></svg>,
  block: () => <svg viewBox="0 0 20 20" fill="none" stroke="#ffb4ab" strokeWidth="1.5" width="20" height="20"><circle cx="10" cy="10" r="8"/><path d="M4 10h12" strokeLinecap="round"/></svg>,
  chevron: () => <svg viewBox="0 0 5 8" fill="none" stroke="currentColor" strokeWidth="1.5" width="5" height="8"><path d="M1 1l3 3-3 3" strokeLinecap="round" strokeLinejoin="round"/></svg>,
  camera: () => <svg viewBox="0 0 20 16" fill="none" stroke="currentColor" strokeWidth="1.5" width="20" height="16"><rect x="1" y="3" width="18" height="12" rx="2"/><circle cx="10" cy="9" r="3.5"/><path d="M6 3l1-2h6l1 2" strokeLinejoin="round"/></svg>,
  text_status: () => <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" strokeWidth="1.5" width="18" height="18"><rect x="1" y="1" width="16" height="16" rx="2"/><path d="M5 6h8M5 9h8M5 12h5" strokeLinecap="round"/></svg>,
  disappear: () => <svg viewBox="0 0 19 22" fill="none" stroke="currentColor" strokeWidth="1.5" width="19" height="22"><path d="M9.5 1C5.36 1 2 4.36 2 8.5c0 5.25 7.5 12.5 7.5 12.5S17 13.75 17 8.5C17 4.36 13.64 1 9.5 1z"/><circle cx="9.5" cy="8.5" r="2.5"/></svg>,
  eye: () => (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  ),
  eyeOff: () => (
    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
      <line x1="1" y1="1" x2="23" y2="23" />
    </svg>
  ),
  connections: () => (
    <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <line x1="19" y1="8" x2="19" y2="14" />
      <line x1="22" y1="11" x2="16" y2="11" />
    </svg>
  ),
};

// ─── Password Strength Validation ─────────────────────────────────────────────
const checkPasswordStrength = (p = "") => {
  const hasMinLen = p.length >= 8;
  const hasUpper = /[A-Z]/.test(p);
  const hasLower = /[a-z]/.test(p);
  const hasNumber = /[0-9]/.test(p);
  const hasSymbol = /[^A-Za-z0-9]/.test(p);
  const isValid = hasMinLen && hasUpper && hasLower && hasNumber && hasSymbol;
  return { hasMinLen, hasUpper, hasLower, hasNumber, hasSymbol, isValid };
};

// ─── Login Page ───────────────────────────────────────────────────────────────
function LoginPage({ onLogin, onGoRegister }) {
  const { C } = useTheme();
  const [email, setEmail] = useState("");
  const [pass, setPass] = useState("");
  const [showPass, setShowPass] = useState(false);
  const [err, setErr] = useState("");

  const reqs = checkPasswordStrength(pass);

  const handle = (e) => {
    e.preventDefault();
    if (!email.trim() || !pass) {
      setErr("Please fill in all fields.");
      return;
    }
    if (!reqs.isValid) {
      setErr("Password must contain at least 8 characters, including 1 uppercase, 1 lowercase, 1 number, and 1 symbol.");
      return;
    }
    setErr("");
    onLogin();
  };

  return (
    <div style={{ minHeight: "100vh", background: C.bg, display: "flex", alignItems: "center", justifyContent: "center", fontFamily: "'Inter', sans-serif" }}>
      <div style={{ width: 420, background: C.sidebar, borderRadius: 20, padding: "40px 36px", boxShadow: "0 20px 40px rgba(0,0,0,0.06)", border: C.boxBorder }}>
        <div style={{ textAlign: "center", marginBottom: 28 }}>
          <div style={{ fontSize: 28, fontWeight: 800, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif", marginBottom: 6 }}>Connectly</div>
          <div style={{ fontSize: 14, color: C.text2 }}>Sign in to your account</div>
        </div>
        {err && (
          <div style={{ background: "rgba(239, 68, 68, 0.12)", color: "#ef4444", borderRadius: 10, padding: "10px 14px", fontSize: 13, marginBottom: 16, border: "1.5px solid #ef4444", lineHeight: 1.4, fontWeight: 600 }}>
            {err}
          </div>
        )}
        <form onSubmit={handle}>
          <label style={{ display: "block", fontSize: 12, color: C.text1, marginBottom: 6, fontWeight: 700 }}>Email or Username</label>
          <input value={email} onChange={e => setEmail(e.target.value)}
            placeholder="hello@example.com"
            style={{ width: "100%", background: C.card, border: C.boxBorder, borderRadius: 12, padding: "11px 14px", color: C.text1, fontSize: 14, marginBottom: 16, boxSizing: "border-box", outline: "none" }} />
          
          <label style={{ display: "block", fontSize: 12, color: C.text1, marginBottom: 6, fontWeight: 700 }}>Password</label>
          <div style={{ position: "relative", marginBottom: 10 }}>
            <input type={showPass ? "text" : "password"} value={pass} onChange={e => setPass(e.target.value)}
              placeholder="••••••••"
              style={{ width: "100%", background: C.card, border: C.boxBorder, borderRadius: 12, padding: "11px 44px 11px 14px", color: C.text1, fontSize: 14, boxSizing: "border-box", outline: "none" }} />
            <button type="button" onClick={() => setShowPass(!showPass)}
              style={{ position: "absolute", right: 14, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", color: C.text2, cursor: "pointer", fontSize: 13, fontWeight: 600 }}>
              {showPass ? "Hide" : "Show"}
            </button>
          </div>

          {/* Real-time Password Requirements Checklist */}
          {pass.length > 0 && (
            <div style={{
              background: C.card,
              borderRadius: 12,
              padding: "10px 14px",
              marginBottom: 14,
              fontSize: 11.5,
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: "6px 8px",
              border: `1px solid ${C.hover}`
            }}>
              <div style={{ color: reqs.hasMinLen ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasMinLen ? 700 : 500 }}>
                <span>{reqs.hasMinLen ? "✓" : "○"}</span> 8+ characters
              </div>
              <div style={{ color: reqs.hasUpper ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasUpper ? 700 : 500 }}>
                <span>{reqs.hasUpper ? "✓" : "○"}</span> Uppercase (A-Z)
              </div>
              <div style={{ color: reqs.hasLower ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasLower ? 700 : 500 }}>
                <span>{reqs.hasLower ? "✓" : "○"}</span> Lowercase (a-z)
              </div>
              <div style={{ color: reqs.hasNumber ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasNumber ? 700 : 500 }}>
                <span>{reqs.hasNumber ? "✓" : "○"}</span> Number (0-9)
              </div>
              <div style={{ color: reqs.hasSymbol ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasSymbol ? 700 : 500, gridColumn: "span 2" }}>
                <span>{reqs.hasSymbol ? "✓" : "○"}</span> Symbol (!@#$%^&*...)
              </div>
            </div>
          )}

          <div style={{ textAlign: "right", marginBottom: 20 }}>
            <span style={{ fontSize: 12, color: C.accent, cursor: "pointer", fontWeight: 600 }}>Forgot password?</span>
          </div>
          <button type="submit" style={{ width: "100%", background: C.accent, color: "#fff", border: C.boxBorder, borderRadius: 12, padding: "13px", fontWeight: 700, fontSize: 15, cursor: "pointer", fontFamily: "'Plus Jakarta Sans', sans-serif", boxShadow: "0 4px 12px rgba(99,102,241,0.25)" }}>
            Sign In
          </button>
        </form>
        <div style={{ textAlign: "center", marginTop: 24, fontSize: 13, color: C.text2 }}>
          Don't have an account?{" "}
          <span onClick={onGoRegister} style={{ color: C.accent, cursor: "pointer", fontWeight: 700 }}>Create one</span>
        </div>
      </div>
    </div>
  );
}

// ─── Register Page ────────────────────────────────────────────────────────────
function RegisterPage({ onRegister, onGoLogin }) {
  const { C, setUserProfile } = useTheme();
  const [form, setForm] = useState({ name: "", username: "", email: "", pass: "", confirm: "" });
  const [showPass, setShowPass] = useState(false);
  const [err, setErr] = useState("");
  const set = k => e => setForm(f => ({ ...f, [k]: e.target.value }));

  const reqs = checkPasswordStrength(form.pass);

  const handle = (e) => {
    e.preventDefault();
    if (!form.name || !form.username || !form.email || !form.pass) {
      setErr("Please fill in all fields.");
      return;
    }
    if (!reqs.isValid) {
      setErr("Password must contain at least 8 characters, including 1 uppercase, 1 lowercase, 1 number, and 1 symbol.");
      return;
    }
    if (form.pass !== form.confirm) {
      setErr("Passwords don't match.");
      return;
    }
    setErr("");
    setUserProfile(prev => ({
      ...prev,
      name: form.name.trim(),
      username: form.username.trim().startsWith("@") ? form.username.trim() : `@${form.username.trim()}`,
    }));
    onRegister();
  };

  return (
    <div style={{ minHeight: "100vh", background: C.bg, display: "flex", alignItems: "center", justifyContent: "center", fontFamily: "'Inter', sans-serif" }}>
      <div style={{ width: 440, background: C.sidebar, borderRadius: 20, padding: "36px 36px", boxShadow: "0 20px 40px rgba(0,0,0,0.06)", border: C.boxBorder }}>
        <div style={{ textAlign: "center", marginBottom: 24 }}>
          <div style={{ fontSize: 26, fontWeight: 800, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif", marginBottom: 6 }}>Create Account</div>
          <div style={{ fontSize: 14, color: C.text2 }}>Join Connectly today</div>
        </div>
        {err && (
          <div style={{ background: "rgba(239, 68, 68, 0.12)", color: "#ef4444", borderRadius: 10, padding: "10px 14px", fontSize: 13, marginBottom: 16, border: "1.5px solid #ef4444", lineHeight: 1.4, fontWeight: 600 }}>
            {err}
          </div>
        )}
        <form onSubmit={handle}>
          <div style={{ marginBottom: 12 }}>
            <label style={{ display: "block", fontSize: 12, color: C.text1, marginBottom: 5, fontWeight: 700 }}>Full Name</label>
            <input type="text" value={form.name} onChange={set("name")} placeholder="Arun Kumar"
              style={{ width: "100%", background: C.card, border: C.boxBorder, borderRadius: 12, padding: "10px 14px", color: C.text1, fontSize: 14, boxSizing: "border-box", outline: "none" }} />
          </div>
          <div style={{ marginBottom: 12 }}>
            <label style={{ display: "block", fontSize: 12, color: C.text1, marginBottom: 5, fontWeight: 700 }}>Username</label>
            <input type="text" value={form.username} onChange={set("username")} placeholder="@arunkumar"
              style={{ width: "100%", background: C.card, border: C.boxBorder, borderRadius: 12, padding: "10px 14px", color: C.text1, fontSize: 14, boxSizing: "border-box", outline: "none" }} />
          </div>
          <div style={{ marginBottom: 12 }}>
            <label style={{ display: "block", fontSize: 12, color: C.text1, marginBottom: 5, fontWeight: 700 }}>Email</label>
            <input type="email" value={form.email} onChange={set("email")} placeholder="hello@example.com"
              style={{ width: "100%", background: C.card, border: C.boxBorder, borderRadius: 12, padding: "10px 14px", color: C.text1, fontSize: 14, boxSizing: "border-box", outline: "none" }} />
          </div>
          <div style={{ marginBottom: 10 }}>
            <label style={{ display: "block", fontSize: 12, color: C.text1, marginBottom: 5, fontWeight: 700 }}>Password</label>
            <div style={{ position: "relative" }}>
              <input type={showPass ? "text" : "password"} value={form.pass} onChange={set("pass")} placeholder="••••••••"
                style={{ width: "100%", background: C.card, border: C.boxBorder, borderRadius: 12, padding: "10px 44px 10px 14px", color: C.text1, fontSize: 14, boxSizing: "border-box", outline: "none" }} />
              <button type="button" onClick={() => setShowPass(!showPass)}
                style={{ position: "absolute", right: 14, top: "50%", transform: "translateY(-50%)", background: "none", border: "none", color: C.text2, cursor: "pointer", fontSize: 13, fontWeight: 600 }}>
                {showPass ? "Hide" : "Show"}
              </button>
            </div>
          </div>

          {/* Real-time Password Requirements Checklist in Registration */}
          {form.pass.length > 0 && (
            <div style={{
              background: C.card,
              borderRadius: 12,
              padding: "10px 14px",
              marginBottom: 12,
              fontSize: 11.5,
              display: "grid",
              gridTemplateColumns: "1fr 1fr",
              gap: "6px 8px",
              border: `1px solid ${C.hover}`
            }}>
              <div style={{ color: reqs.hasMinLen ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasMinLen ? 700 : 500 }}>
                <span>{reqs.hasMinLen ? "✓" : "○"}</span> 8+ characters
              </div>
              <div style={{ color: reqs.hasUpper ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasUpper ? 700 : 500 }}>
                <span>{reqs.hasUpper ? "✓" : "○"}</span> Uppercase (A-Z)
              </div>
              <div style={{ color: reqs.hasLower ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasLower ? 700 : 500 }}>
                <span>{reqs.hasLower ? "✓" : "○"}</span> Lowercase (a-z)
              </div>
              <div style={{ color: reqs.hasNumber ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasNumber ? 700 : 500 }}>
                <span>{reqs.hasNumber ? "✓" : "○"}</span> Number (0-9)
              </div>
              <div style={{ color: reqs.hasSymbol ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 5, fontWeight: reqs.hasSymbol ? 700 : 500, gridColumn: "span 2" }}>
                <span>{reqs.hasSymbol ? "✓" : "○"}</span> Symbol (!@#$%^&*...)
              </div>
            </div>
          )}

          <div style={{ marginBottom: 16 }}>
            <label style={{ display: "block", fontSize: 12, color: C.text1, marginBottom: 5, fontWeight: 700 }}>Confirm Password</label>
            <input type="password" value={form.confirm} onChange={set("confirm")} placeholder="••••••••"
              style={{ width: "100%", background: C.card, border: C.boxBorder, borderRadius: 12, padding: "10px 14px", color: C.text1, fontSize: 14, boxSizing: "border-box", outline: "none" }} />
          </div>

          <button type="submit" style={{ width: "100%", background: C.accent, color: "#fff", border: C.boxBorder, borderRadius: 12, padding: "13px", fontWeight: 700, fontSize: 15, cursor: "pointer", marginTop: 4, fontFamily: "'Plus Jakarta Sans', sans-serif", boxShadow: "0 4px 12px rgba(99,102,241,0.25)" }}>
            Create Account
          </button>
        </form>
        <div style={{ textAlign: "center", marginTop: 18, fontSize: 13, color: C.text2 }}>
          Already have an account?{" "}
          <span onClick={onGoLogin} style={{ color: C.accent, cursor: "pointer", fontWeight: 700 }}>Sign in</span>
        </div>
      </div>
    </div>
  );
}

// ─── Message Hover Action Button ──────────────────────────────────────────────
function MessageHoverActionBtn({ icon: Icon, title, onClick, isDanger, isActive, C }) {
  const [hover, setHover] = useState(false);
  const isDark = C.name === "dark";

  return (
    <button
      type="button"
      onClick={(e) => {
        e.stopPropagation();
        e.preventDefault();
        if (onClick) onClick(e);
      }}
      onMouseDown={(e) => {
        e.stopPropagation();
      }}
      title={title}
      onMouseEnter={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
      style={{
        background: hover
          ? isDanger
            ? "rgba(239, 68, 68, 0.16)"
            : isDark
            ? "rgba(255, 255, 255, 0.14)"
            : "rgba(99, 102, 241, 0.12)"
          : isActive
          ? isDark ? "rgba(245, 158, 11, 0.2)" : "rgba(245, 158, 11, 0.15)"
          : "transparent",
        border: "none",
        color: hover
          ? isDanger
            ? "#ef4444"
            : isActive
            ? "#f59e0b"
            : C.accent
          : isActive
          ? "#f59e0b"
          : C.text2,
        cursor: "pointer",
        width: 28,
        height: 28,
        borderRadius: "50%",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        transition: "all 0.18s cubic-bezier(0.4, 0, 0.2, 1)",
        transform: hover ? "scale(1.15)" : "scale(1)",
        outline: "none",
        padding: 0,
      }}
    >
      <Icon />
    </button>
  );
}

// ─── Message Bubble ────────────────────────────────────────────────────────────
function MessageBubble({ msg, onReply, onForward, onDelete, onCopy, onToggleStar, chatUsers, isReplyingTo, isHighlighted, onScrollToReply, selectionMode, isSelected, onToggleSelect }) {
  const { C } = useTheme();
  const [showMenu, setShowMenu] = useState(false);
  const isMine = msg.from === "me";
  const sender = getUser(msg.from);
  const isDark = C.name === "dark";

  const menuStyle = {
    position: "absolute",
    top: "-14px",
    background: isDark ? "rgba(23, 34, 59, 0.96)" : "rgba(255, 255, 255, 0.96)",
    backdropFilter: "blur(12px)",
    WebkitBackdropFilter: "blur(12px)",
    border: `1px solid ${isDark ? "rgba(255, 255, 255, 0.14)" : "rgba(0, 0, 0, 0.08)"}`,
    borderRadius: 24,
    padding: "3px 5px",
    display: "flex",
    alignItems: "center",
    gap: 3,
    zIndex: 40,
    boxShadow: isDark
      ? "0 8px 24px -4px rgba(0,0,0,0.6), 0 2px 6px rgba(0,0,0,0.4)"
      : "0 8px 20px -4px rgba(0,0,0,0.12), 0 2px 6px rgba(0,0,0,0.06)",
    ...(isMine ? { right: 8 } : { left: 8 }),
    animation: "fadeIn 0.15s ease-out"
  };

  const bubbleBg = isMine ? C.accent : C.card;
  const textCol = isMine ? C.accentDk : C.text1;

  const renderReplyQuote = (reply) => {
    if (!reply) return null;
    return (
      <div
        onClick={(e) => {
          e.stopPropagation();
          if (onScrollToReply && (reply.targetId || reply.id)) {
            onScrollToReply(reply.targetId || reply.id);
          }
        }}
        title="Click to jump to original message"
        style={{
          background: isMine
            ? (isDark ? "rgba(0, 0, 0, 0.24)" : "rgba(255, 255, 255, 0.24)")
            : (isDark ? "rgba(255, 255, 255, 0.08)" : "rgba(99, 102, 241, 0.08)"),
          borderLeft: `4px solid ${isMine ? "#ffffff" : C.accent}`,
          borderRadius: "6px 8px 8px 6px",
          padding: "5px 9px",
          marginBottom: 6,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: 8,
          maxWidth: "100%",
          overflow: "hidden",
          cursor: "pointer",
          transition: "all 0.15s ease",
        }}>
        <div style={{ minWidth: 0, flex: 1 }}>
          <div style={{
            fontSize: 11,
            fontWeight: 700,
            color: isMine ? "#ffffff" : C.accentMid,
            marginBottom: 2,
            display: "flex",
            alignItems: "center",
            gap: 4
          }}>
            <span style={{ fontSize: 10 }}>↩</span> {reply.name}
          </div>
          <div style={{
            fontSize: 12,
            color: isMine ? "rgba(255, 255, 255, 0.92)" : C.text2,
            whiteSpace: "nowrap",
            overflow: "hidden",
            textOverflow: "ellipsis"
          }}>
            {reply.text || (reply.type === "image" ? "📷 Photo" : reply.type === "video" ? "🎥 Video" : "Message")}
          </div>
        </div>
        {reply.thumb && (
          <img src={reply.thumb} alt="" style={{ width: 32, height: 32, borderRadius: 5, objectFit: "cover", flexShrink: 0 }} />
        )}
      </div>
    );
  };

  return (
    <div
      id={`msg-${msg.id}`}
      onClick={() => {
        if (selectionMode && onToggleSelect) onToggleSelect();
      }}
      style={{
        display: "flex",
        justifyContent: isMine ? "flex-end" : "flex-start",
        alignItems: "flex-end",
        gap: 8,
        marginBottom: 4,
        position: "relative",
        padding: "4px 8px",
        borderRadius: 14,
        cursor: selectionMode ? "pointer" : "default",
        transition: "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)",
        ...(isSelected ? {
          background: isDark ? "rgba(99, 102, 241, 0.22)" : "rgba(99, 102, 241, 0.12)",
          boxShadow: `0 0 0 1.5px ${C.accent}`,
        } : isHighlighted ? {
          background: isDark ? "rgba(99, 102, 241, 0.26)" : "rgba(99, 102, 241, 0.18)",
          boxShadow: `0 0 0 2px ${C.accent}, 0 4px 16px rgba(99,102,241,0.25)`,
          transform: "scale(1.02)",
        } : isReplyingTo ? {
          background: isDark ? "rgba(99, 102, 241, 0.12)" : "rgba(99, 102, 241, 0.08)",
          boxShadow: `0 0 0 1.5px ${C.accent}`,
        } : {})
      }}
      onMouseEnter={() => !selectionMode && setShowMenu(true)}
      onMouseLeave={() => setShowMenu(false)}
    >
      {/* Checkbox indicator in Selection Mode */}
      {selectionMode && (
        <div
          onClick={(e) => {
            e.stopPropagation();
            if (onToggleSelect) onToggleSelect();
          }}
          style={{
            width: 20,
            height: 20,
            borderRadius: "50%",
            border: `2px solid ${isSelected ? C.accent : C.text2}`,
            background: isSelected ? C.accent : "transparent",
            color: "#fff",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            fontSize: 11,
            fontWeight: 800,
            flexShrink: 0,
            marginBottom: 8,
            cursor: "pointer",
            transition: "all 0.15s ease",
            order: isMine ? 2 : -1
          }}>
          {isSelected ? "✓" : ""}
        </div>
      )}

      {/* Replying point-out badge when active */}
      {isReplyingTo && (
        <div style={{
          position: "absolute",
          top: -10,
          left: isMine ? "auto" : 40,
          right: isMine ? 40 : "auto",
          background: C.accent,
          color: "#fff",
          fontSize: 10,
          fontWeight: 700,
          padding: "2px 8px",
          borderRadius: 99,
          display: "flex",
          alignItems: "center",
          gap: 4,
          boxShadow: "0 2px 8px rgba(0,0,0,0.25)",
          zIndex: 30,
          animation: "fadeIn 0.2s ease-out"
        }}>
          <span>↩</span> Replying
        </div>
      )}

      {!isMine && <Avatar user={sender} size={28} />}

      <div style={{ position: "relative", maxWidth: 340 }}>
        {showMenu && (
          <div style={menuStyle}>
            <MessageHoverActionBtn icon={Ic.reply} title="Reply" onClick={() => { onReply(msg); setShowMenu(false); }} C={C} />
            <MessageHoverActionBtn icon={Ic.forward} title="Forward" onClick={() => { onForward(msg); setShowMenu(false); }} C={C} />
            <MessageHoverActionBtn icon={Ic.star} title={msg.starred ? "Unstar message" : "Star message"} isActive={msg.starred} onClick={() => { onToggleStar?.(msg.id); setShowMenu(false); }} C={C} />
            <MessageHoverActionBtn icon={Ic.copy} title="Copy text" onClick={() => { onCopy(msg); setShowMenu(false); }} C={C} />
            <MessageHoverActionBtn icon={Ic.trash} title="Delete message" isDanger={true} onClick={() => { onDelete(msg.id); setShowMenu(false); }} C={C} />
          </div>
        )}

        {/* Sticker */}
        {msg.type === "sticker" && (
          <div style={{ background: C.panel, borderRadius: 16, padding: 8, display: "inline-flex", flexDirection: "column", alignItems: "center", border: isDark ? "1px solid rgba(255,255,255,0.06)" : "none" }}>
            {msg.replyTo && <div style={{ width: "100%", marginBottom: 4 }}>{renderReplyQuote(msg.replyTo)}</div>}
            <div style={{ background: C.card, borderRadius: 12, padding: 8, fontSize: 56, lineHeight: 1 }}>{msg.emoji}</div>
            <div style={{ fontSize: 11, color: C.text2, marginTop: 4, display: "flex", gap: 5, alignItems: "center" }}>
              {msg.starred && <span title="Starred message" style={{ color: "#f59e0b", fontSize: 11 }}>★</span>}
              <span>{msg.time}</span>
              {isMine && <CheckIcon status={msg.status} />}
            </div>
          </div>
        )}

        {/* Image */}
        {msg.type === "image" && (
          <div style={{
            background: isMine ? bubbleBg : C.card,
            borderRadius: isMine ? "18px 4px 18px 18px" : "4px 18px 18px 18px",
            overflow: "hidden",
            boxShadow: isDark ? "0 4px 14px rgba(0,0,0,0.3)" : "0 2px 8px rgba(0,0,0,0.06)",
            maxWidth: 300,
            border: isDark ? "1px solid rgba(255,255,255,0.08)" : "1px solid rgba(0,0,0,0.04)"
          }}>
            {msg.replyTo && <div style={{ padding: "8px 8px 0" }}>{renderReplyQuote(msg.replyTo)}</div>}
            <div style={{ position: "relative", overflow: "hidden" }}>
              <img src={msg.url} alt="" style={{ width: "100%", maxHeight: 240, objectFit: "cover", display: "block" }} />
              <div style={{
                position: "absolute",
                bottom: 6,
                right: 8,
                background: "rgba(10, 16, 32, 0.72)",
                backdropFilter: "blur(6px)",
                WebkitBackdropFilter: "blur(6px)",
                borderRadius: 99,
                padding: "3px 8px",
                fontSize: 11,
                color: "#ffffff",
                fontWeight: 600,
                display: "flex",
                alignItems: "center",
                gap: 4
              }}>
                {msg.starred && <span title="Starred message" style={{ color: "#fbbf24", fontSize: 11 }}>★</span>}
                <span>{msg.time}</span>
                {isMine && <CheckIcon status={msg.status} light />}
              </div>
            </div>
            {msg.caption && (
              <div style={{ padding: "8px 12px 10px", fontSize: 13.5, color: isMine ? C.accentDk : C.text1, lineHeight: 1.5, wordBreak: "break-word" }}>
                {msg.caption}
              </div>
            )}
          </div>
        )}

        {/* Video */}
        {msg.type === "video" && (
          <div style={{
            background: isMine ? bubbleBg : C.card,
            borderRadius: isMine ? "18px 4px 18px 18px" : "4px 18px 18px 18px",
            overflow: "hidden",
            position: "relative",
            width: 280,
            boxShadow: isDark ? "0 4px 14px rgba(0,0,0,0.3)" : "0 2px 8px rgba(0,0,0,0.06)",
            border: isDark ? "1px solid rgba(255,255,255,0.08)" : "1px solid rgba(0,0,0,0.04)"
          }}>
            {msg.replyTo && <div style={{ padding: "8px 8px 0" }}>{renderReplyQuote(msg.replyTo)}</div>}
            <div style={{ position: "relative", height: 160 }}>
              <img src={msg.thumb} alt="" style={{ width: "100%", height: "100%", objectFit: "cover", display: "block" }} />
              <div style={{ position: "absolute", inset: 0, background: "linear-gradient(to top, rgba(6,14,32,0.7) 0%, transparent 50%)" }} />
              <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center" }}>
                <div style={{ background: C.accent, borderRadius: "50%", width: 44, height: 44, display: "flex", alignItems: "center", justifyContent: "center", boxShadow: "0 4px 12px rgba(0,0,0,0.4)" }}>
                  <Ic.play />
                </div>
              </div>
              <div style={{ position: "absolute", bottom: 8, left: 8, background: "rgba(6,14,32,0.8)", backdropFilter: "blur(4px)", borderRadius: 6, padding: "2px 8px", fontSize: 12, color: "#ffffff", display: "flex", gap: 4, alignItems: "center" }}>
                <span style={{ fontSize: 10 }}>▶</span>{msg.duration}
              </div>
              <div style={{ position: "absolute", bottom: 8, right: 8, background: "rgba(6,14,32,0.8)", backdropFilter: "blur(4px)", borderRadius: 99, padding: "3px 8px", fontSize: 11, color: "#ffffff", fontWeight: 600, display: "flex", alignItems: "center", gap: 4 }}>
                {msg.starred && <span title="Starred message" style={{ color: "#fbbf24", fontSize: 11 }}>★</span>}
                <span>{msg.time}</span>
                {isMine && <CheckIcon status={msg.status} light />}
              </div>
            </div>
          </div>
        )}

        {/* Text */}
        {msg.type === "text" && (
          <div style={{
            background: bubbleBg,
            borderRadius: isMine ? "18px 4px 18px 18px" : "4px 18px 18px 18px",
            padding: "10px 14px 8px",
            maxWidth: 320,
            boxShadow: isDark ? "0 2px 8px rgba(0,0,0,0.2)" : "0 1px 2px rgba(0,0,0,0.05)",
            border: isDark && !isMine ? "1px solid rgba(255,255,255,0.06)" : "none"
          }}>
            {msg.forwarded && (
              <div style={{ display: "flex", gap: 5, alignItems: "center", marginBottom: 4, opacity: 0.75 }}>
                <Ic.forward /><span style={{ fontSize: 11, fontStyle: "italic", color: isMine ? C.accentDk : C.text2, fontWeight: 600 }}>Forwarded</span>
              </div>
            )}
            {msg.replyTo && renderReplyQuote(msg.replyTo)}
            <div style={{ fontSize: 14, color: textCol, lineHeight: 1.55, wordBreak: "break-word" }}>{msg.text}</div>
            <div style={{ display: "flex", justifyContent: "flex-end", gap: 4, alignItems: "center", marginTop: 4 }}>
              {msg.starred && <span title="Starred message" style={{ color: isMine ? "#fde047" : "#f59e0b", fontSize: 11 }}>★</span>}
              <span style={{ fontSize: 11, color: isMine ? "rgba(255,255,255,0.8)" : C.text2, fontWeight: 600 }}>{msg.time}</span>
              {isMine && <CheckIcon status={msg.status} />}
            </div>
          </div>
        )}
      </div>

      {isMine && <Avatar user={CURRENT_USER} size={28} />}
    </div>
  );
}

// ─── Chat View ────────────────────────────────────────────────────────────────
function ChatView({ chat, chats, onUpdateChat, onDeleteChat, onBack, isMobile }) {
  const { C, lastSeenPrivacy, profilePhotoPrivacy } = useTheme();
  const isDark = C.name === "dark";
  const [text, setText] = useState("");
  const [replyTo, setReplyTo] = useState(null);
  const [highlightedMsgId, setHighlightedMsgId] = useState(null);
  const [selectionMode, setSelectionMode] = useState(false);
  const [selectedMsgIds, setSelectedMsgIds] = useState([]);
  const [showStickers, setShowStickers] = useState(false);
  const [showForward, setShowForward] = useState(null);
  const [showInfo, setShowInfo] = useState(true);
  const [showStarredModal, setShowStarredModal] = useState(false);
  const [toastMsg, setToastMsg] = useState("");
  const [muteNotifs, setMuteNotifs] = useState(false);
  const [showMoreMenu, setShowMoreMenu] = useState(false);
  const [inChatSearchOpen, setInChatSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [confirmModal, setConfirmModal] = useState(null); // null | "clear" | "delete" | "block"
  const [blockedUsers, setBlockedUsers] = useState([]);
  const bottomRef = useRef();
  const inputRef = useRef(null);

  const scrollToMessage = (targetMsgId) => {
    if (!targetMsgId) return;
    const el = document.getElementById(`msg-${targetMsgId}`);
    if (el) {
      el.scrollIntoView({ behavior: "smooth", block: "center" });
      setHighlightedMsgId(targetMsgId);
      setTimeout(() => {
        setHighlightedMsgId(null);
      }, 1600);
    }
  };

  const handleReply = (targetMsg) => {
    if (!targetMsg) return;
    setReplyTo(targetMsg);
    setShowStickers(false);
    setHighlightedMsgId(targetMsg.id);
    setTimeout(() => {
      setHighlightedMsgId(null);
    }, 1500);

    setTimeout(() => {
      if (inputRef.current) {
        inputRef.current.focus();
        inputRef.current.scrollIntoView({ behavior: "smooth", block: "nearest" });
      }
    }, 40);
  };

  useEffect(() => {
    if (replyTo && inputRef.current) {
      inputRef.current.focus();
    }
  }, [replyTo]);

  const isGroup = chat.type === "group";
  const chatUser = !isGroup ? getUser(chat.userId) : null;
  const isBlocked = !isGroup && blockedUsers.includes(chatUser?.id);
  const messages = chat.messages || [];
  const starredMessages = messages.filter(m => m.starred);
  const allSelectedAreStarred = selectedMsgIds.length > 0 && selectedMsgIds.every(id => messages.find(m => m.id === id)?.starred);

  const toggleStarSelected = () => {
    if (selectedMsgIds.length === 0) return;
    const shouldStar = !allSelectedAreStarred;
    const updatedMessages = messages.map(m => {
      if (selectedMsgIds.includes(m.id)) {
        return { ...m, starred: shouldStar };
      }
      return m;
    });
    onUpdateChat(chat.id, updatedMessages);
    const count = selectedMsgIds.length;
    setToastMsg(`⭐ ${count} message${count > 1 ? "s" : ""} ${shouldStar ? "starred" : "unstarred"}`);
    setTimeout(() => setToastMsg(""), 2500);
    setSelectedMsgIds([]);
    setSelectionMode(false);
  };

  const toggleStarMsg = (msgId) => {
    let wasStarred = false;
    const updatedMessages = messages.map(m => {
      if (m.id === msgId) {
        wasStarred = !m.starred;
        return { ...m, starred: wasStarred };
      }
      return m;
    });
    onUpdateChat(chat.id, updatedMessages);
    setToastMsg(wasStarred ? "⭐ Message starred" : "Message unstarred");
    setTimeout(() => setToastMsg(""), 2200);
  };

  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: "smooth" }); }, [messages.length]);

  const send = (msgData) => {
    if (isBlocked) return;
    const now = new Date();
    const time = now.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    const newMsg = { id: `m${Date.now()}`, from: "me", time, status: "sent", ...msgData };
    onUpdateChat(chat.id, [...messages, newMsg]);
    setText("");
    setReplyTo(null);
    setShowStickers(false);
  };

  const getReplyPayload = (targetMsg) => {
    if (!targetMsg) return null;
    const isSelf = targetMsg.from === "me";
    const authorName = isSelf ? "You" : (getUser(targetMsg.from)?.name || "User");
    let preview = "Message";
    if (targetMsg.type === "text") preview = targetMsg.text || "";
    else if (targetMsg.type === "image") preview = targetMsg.caption ? `📷 ${targetMsg.caption}` : "📷 Photo";
    else if (targetMsg.type === "video") preview = "🎥 Video";
    else if (targetMsg.type === "sticker") preview = `Sticker ${targetMsg.emoji || ""}`;

    return {
      id: targetMsg.id,
      targetId: targetMsg.id,
      name: authorName,
      text: preview,
      type: targetMsg.type,
      thumb: targetMsg.url || targetMsg.thumb || null
    };
  };

  const sendText = () => {
    if (!text.trim() || isBlocked) return;
    const replyData = getReplyPayload(replyTo);
    send({
      type: "text",
      text: text.trim(),
      ...(replyData ? { replyTo: replyData } : {})
    });
  };

  const handleMediaUpload = (e) => {
    if (isBlocked) return;
    const file = e.target.files[0];
    if (!file) return;
    const url = URL.createObjectURL(file);
    const replyData = getReplyPayload(replyTo);
    if (file.type.startsWith("image/")) {
      send({ type: "image", url, caption: "", ...(replyData ? { replyTo: replyData } : {}) });
    } else if (file.type.startsWith("video/")) {
      send({ type: "video", thumb: url, duration: "0:00", ...(replyData ? { replyTo: replyData } : {}) });
    }
  };

  const deleteMsg = (id) => onUpdateChat(chat.id, messages.filter(m => m.id !== id));
  const copyMsg = (msg) => msg.text && navigator.clipboard?.writeText(msg.text);

  const headerName = isGroup ? chat.name : chatUser?.name;

  // Last Seen Privacy computation:
  let headerSub = "";
  let showHeaderDot = false;
  let isUserOnline = false;

  if (isGroup) {
    headerSub = `${chat.members.length} members`;
  } else if (isBlocked) {
    headerSub = "Blocked";
    showHeaderDot = false;
  } else {
    if (lastSeenPrivacy === "Nobody") {
      headerSub = chatUser?.isContact ? "Contact" : (chatUser?.username || "");
      showHeaderDot = false;
    } else if (lastSeenPrivacy === "My Contacts") {
      if (chatUser?.isContact) {
        isUserOnline = !!chatUser.online;
        showHeaderDot = true;
        headerSub = isUserOnline ? "Online • Active now" : `Last seen ${chatUser.lastSeen || "recently"}`;
      } else {
        headerSub = chatUser?.username || "Not in contacts";
        showHeaderDot = false;
      }
    } else { // "Everyone"
      isUserOnline = !!chatUser?.online;
      showHeaderDot = true;
      headerSub = isUserOnline ? "Online • Active now" : `Last seen ${chatUser?.lastSeen || "recently"}`;
    }
  }

  const headerAvatar = isGroup
    ? <div style={{ width: 40, height: 40, borderRadius: "50%", background: chat.color, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 14, fontWeight: 700, color: C.text3, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>{chat.initials}</div>
    : <Avatar user={chatUser} size={40} showOnline={!isBlocked} />;

  // Filter messages for search query
  const displayMessages = searchQuery.trim()
    ? messages.filter(m => (m.text || "").toLowerCase().includes(searchQuery.toLowerCase()))
    : messages;

  // shared media images from messages
  const sharedImages = messages.filter(m => m.type === "image" || m.type === "video");

  return (
    <div style={{ display: "flex", flex: 1, height: "100%", overflow: "hidden", position: "relative" }}>
      {/* Toast Notification */}
      {toastMsg && (
        <div style={{
          position: "absolute",
          top: 72,
          left: "50%",
          transform: "translateX(-50%)",
          background: isDark ? "rgba(30, 44, 76, 0.96)" : "rgba(15, 23, 42, 0.94)",
          backdropFilter: "blur(12px)",
          color: "#ffffff",
          fontSize: 13,
          fontWeight: 700,
          padding: "7px 18px",
          borderRadius: 99,
          zIndex: 200,
          boxShadow: "0 10px 25px rgba(0,0,0,0.3)",
          display: "flex",
          alignItems: "center",
          gap: 6,
          border: `1px solid ${C.hover}`,
          animation: "fadeIn 0.2s ease-out"
        }}>
          {toastMsg}
        </div>
      )}

      {/* Main chat area */}
      <div style={{ display: "flex", flexDirection: "column", flex: 1, overflow: "hidden" }}>
        {/* Header */}
        <div style={{ background: C.panel, height: 64, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 24px", flexShrink: 0, boxShadow: "0 1px 1px rgba(0,0,0,0.05)", position: "relative", zIndex: 30 }}>
          <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
            {isMobile && (
              <button onClick={onBack} style={{ background: "none", border: "none", color: C.text2, cursor: "pointer", fontSize: 20, padding: 0 }}>←</button>
            )}
            {headerAvatar}
            <div>
              <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                <span style={{ fontWeight: 700, fontSize: 16, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>{headerName}</span>
                <span style={{ background: C.hover, borderRadius: 4, padding: "1px 6px", fontSize: 11, color: C.text2 }}>{isGroup ? "Group" : "Direct"}</span>
              </div>
              <div style={{ display: "flex", gap: 6, alignItems: "center", marginTop: 2 }}>
                {!isGroup && showHeaderDot && (
                  <div style={{ width: 6, height: 6, borderRadius: "50%", background: isUserOnline ? C.online : C.offline }} />
                )}
                <span style={{ fontSize: 12, color: (showHeaderDot && isUserOnline) ? C.accentMid : C.text2, fontWeight: 500 }}>{headerSub}</span>
              </div>
            </div>
          </div>
          <div style={{ display: "flex", gap: 4, alignItems: "center", position: "relative" }}>
            <button
              style={{ background: inChatSearchOpen ? C.hover : "none", border: "none", color: inChatSearchOpen ? C.accent : C.text2, cursor: "pointer", padding: "8px", borderRadius: 10 }}
              title="Search" onClick={() => { setInChatSearchOpen(!inChatSearchOpen); setShowMoreMenu(false); }}>
              <Ic.search />
            </button>

            {/* Three Dot Button & Dropdown Menu */}
            <div style={{ position: "relative" }}>
              <button
                style={{ background: showMoreMenu ? C.hover : "none", border: "none", color: showMoreMenu ? C.accent : C.text2, cursor: "pointer", padding: "8px", borderRadius: 10, display: "flex", alignItems: "center", justifyContent: "center" }}
                title="More options" onClick={() => setShowMoreMenu(!showMoreMenu)}>
                <Ic.more />
              </button>

              {showMoreMenu && (
                <>
                  <div onClick={() => setShowMoreMenu(false)} style={{ position: "fixed", inset: 0, zIndex: 90 }} />
                  <div style={{
                    position: "absolute",
                    top: 42,
                    right: 0,
                    width: 200,
                    background: C.sidebar,
                    borderRadius: 14,
                    padding: 6,
                    boxShadow: "0 14px 34px rgba(0,0,0,0.22)",
                    border: `1px solid ${C.hover}`,
                    zIndex: 100,
                    display: "flex",
                    flexDirection: "column",
                    gap: 2
                  }}>
                    <button onClick={() => { setShowMoreMenu(false); setInChatSearchOpen(true); }}
                      style={{ width: "100%", textAlign: "left", background: "none", border: "none", padding: "9px 12px", borderRadius: 8, fontSize: 13, fontWeight: 600, color: C.text1, cursor: "pointer", display: "flex", gap: 10, alignItems: "center" }}>
                      <Ic.search /> Search
                    </button>
                    <button onClick={() => { setShowMoreMenu(false); setSelectionMode(true); }}
                      style={{ width: "100%", textAlign: "left", background: "none", border: "none", padding: "9px 12px", borderRadius: 8, fontSize: 13, fontWeight: 600, color: C.text1, cursor: "pointer", display: "flex", gap: 10, alignItems: "center" }}>
                      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" width="16" height="16"><rect x="3" y="3" width="18" height="18" rx="4"/><path d="m9 12 2 2 4-4"/></svg> Select chat
                    </button>
                    <button onClick={() => { setShowMoreMenu(false); onBack(); }}
                      style={{ width: "100%", textAlign: "left", background: "none", border: "none", padding: "9px 12px", borderRadius: 8, fontSize: 13, fontWeight: 600, color: C.text1, cursor: "pointer", display: "flex", gap: 10, alignItems: "center" }}>
                      <span style={{ fontSize: 14, width: 16, display: "inline-block", textAlign: "center" }}>✕</span> Close chat
                    </button>
                    <button onClick={() => { setShowMoreMenu(false); setConfirmModal("clear"); }}
                      style={{ width: "100%", textAlign: "left", background: "none", border: "none", padding: "9px 12px", borderRadius: 8, fontSize: 13, fontWeight: 600, color: C.text1, cursor: "pointer", display: "flex", gap: 10, alignItems: "center" }}>
                      <span style={{ fontSize: 14, width: 16, display: "inline-block", textAlign: "center" }}>🧹</span> Clear chat
                    </button>
                    <button onClick={() => { setShowMoreMenu(false); setConfirmModal("delete"); }}
                      style={{ width: "100%", textAlign: "left", background: "none", border: "none", padding: "9px 12px", borderRadius: 8, fontSize: 13, fontWeight: 600, color: C.danger, cursor: "pointer", display: "flex", gap: 10, alignItems: "center" }}>
                      <span style={{ fontSize: 14, width: 16, display: "inline-block", textAlign: "center" }}>🗑️</span> Delete chat
                    </button>
                    {!isGroup && (
                      <button onClick={() => { setShowMoreMenu(false); setConfirmModal("block"); }}
                        style={{ width: "100%", textAlign: "left", background: "none", border: "none", padding: "9px 12px", borderRadius: 8, fontSize: 13, fontWeight: 600, color: C.danger, cursor: "pointer", display: "flex", gap: 10, alignItems: "center" }}>
                        <span style={{ fontSize: 14, width: 16, display: "inline-block", textAlign: "center" }}>🚫</span> {isBlocked ? `Unblock ${headerName}` : `Block ${headerName}`}
                      </button>
                    )}
                  </div>
                </>
              )}
            </div>

            <button
              style={{ background: showInfo ? C.hover : "none", border: "none", color: C.text2, cursor: "pointer", padding: "8px", borderRadius: 10 }}
              title="Info" onClick={() => setShowInfo(!showInfo)}>
              <Ic.profile />
            </button>
          </div>
        </div>

        {/* Selection Mode Action Bar */}
        {selectionMode && (
          <div style={{
            background: isDark ? "rgba(23, 34, 59, 0.98)" : "rgba(241, 245, 249, 0.98)",
            backdropFilter: "blur(12px)",
            padding: "8px 20px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            borderBottom: `1px solid ${C.hover}`,
            zIndex: 25,
            animation: "fadeIn 0.15s ease-out"
          }}>
            <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
              <button
                onClick={() => { setSelectionMode(false); setSelectedMsgIds([]); }}
                style={{ background: "none", border: "none", color: C.text2, cursor: "pointer", fontSize: 16, padding: "2px 6px", display: "flex", alignItems: "center" }}
                title="Exit selection">
                ✕
              </button>
              <span style={{ fontSize: 13, fontWeight: 700, color: C.text1 }}>
                {selectedMsgIds.length === 0 ? "Select messages" : `${selectedMsgIds.length} selected`}
              </span>
              <button
                onClick={() => setSelectedMsgIds(selectedMsgIds.length === messages.length ? [] : messages.map(m => m.id))}
                style={{ background: C.card, border: `1px solid ${C.hover}`, color: C.accent, borderRadius: 8, padding: "3px 10px", fontSize: 12, fontWeight: 600, cursor: "pointer" }}>
                {selectedMsgIds.length === messages.length ? "Deselect all" : "Select all"}
              </button>
            </div>
            {selectedMsgIds.length > 0 && (
              <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                <button
                  onClick={() => {
                    const firstSelected = messages.find(m => selectedMsgIds.includes(m.id));
                    if (firstSelected) setShowForward(firstSelected);
                  }}
                  style={{ background: C.card, border: `1px solid ${C.hover}`, color: C.text1, borderRadius: 8, padding: "4px 12px", fontSize: 12, fontWeight: 600, cursor: "pointer", display: "flex", gap: 6, alignItems: "center" }}>
                  <Ic.forward /> Forward ({selectedMsgIds.length})
                </button>
                <button
                  onClick={toggleStarSelected}
                  style={{ background: C.card, border: `1px solid ${C.hover}`, color: allSelectedAreStarred ? C.text2 : "#f59e0b", borderRadius: 8, padding: "4px 12px", fontSize: 12, fontWeight: 600, cursor: "pointer", display: "flex", gap: 6, alignItems: "center" }}
                  title={allSelectedAreStarred ? "Unstar messages" : "Star messages"}>
                  <Ic.star /> {allSelectedAreStarred ? "Unstar" : "Star"} ({selectedMsgIds.length})
                </button>
                <button
                  onClick={() => {
                    onUpdateChat(chat.id, messages.filter(m => !selectedMsgIds.includes(m.id)));
                    setSelectedMsgIds([]);
                    setSelectionMode(false);
                  }}
                  style={{ background: "rgba(239, 68, 68, 0.12)", border: "none", color: C.danger, borderRadius: 8, padding: "4px 12px", fontSize: 12, fontWeight: 600, cursor: "pointer", display: "flex", gap: 6, alignItems: "center" }}>
                  <Ic.trash /> Delete ({selectedMsgIds.length})
                </button>
              </div>
            )}
          </div>
        )}

        {/* In-Chat Message Search Bar */}
        {inChatSearchOpen && (
          <div style={{ background: C.card, padding: "8px 18px", display: "flex", gap: 10, alignItems: "center", borderBottom: `1px solid ${C.hover}`, flexShrink: 0, zIndex: 20 }}>
            <div style={{ color: C.text2, display: "flex", alignItems: "center" }}><Ic.search /></div>
            <input
              autoFocus
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              placeholder="Search in conversation..."
              style={{ flex: 1, background: "none", border: "none", color: C.text1, fontSize: 13, outline: "none" }}
            />
            {searchQuery.trim() && (
              <span style={{ fontSize: 11, color: C.text2, fontWeight: 600 }}>
                {displayMessages.length} found
              </span>
            )}
            <button onClick={() => { setInChatSearchOpen(false); setSearchQuery(""); }}
              style={{ background: "none", border: "none", color: C.text2, cursor: "pointer", fontSize: 14, padding: "2px 6px" }}>
              ✕
            </button>
          </div>
        )}

        {/* Messages */}
        <div style={{ flex: 1, overflowY: "auto", padding: "16px 24px", display: "flex", flexDirection: "column", gap: 6 }}>
          <div style={{ display: "flex", justifyContent: "center", marginBottom: 12 }}>
            <span style={{ background: C.sidebar, borderRadius: 99, padding: "2px 12px", fontSize: 11, color: C.text2, fontWeight: 600 }}>Today</span>
          </div>

          {searchQuery.trim() && displayMessages.length === 0 && (
            <div style={{ textAlign: "center", color: C.text2, fontSize: 13, margin: "24px 0" }}>
              No messages found matching "{searchQuery}"
            </div>
          )}

          {displayMessages.map(msg => (
            <MessageBubble
              key={msg.id} msg={msg}
              onReply={handleReply}
              onForward={setShowForward}
              onDelete={deleteMsg}
              onCopy={copyMsg}
              onToggleStar={toggleStarMsg}
              chatUsers={[chatUser]}
              isReplyingTo={replyTo?.id === msg.id}
              isHighlighted={highlightedMsgId === msg.id}
              onScrollToReply={scrollToMessage}
              selectionMode={selectionMode}
              isSelected={selectedMsgIds.includes(msg.id)}
              onToggleSelect={() => setSelectedMsgIds(prev => prev.includes(msg.id) ? prev.filter(id => id !== msg.id) : [...prev, msg.id])}
            />
          ))}
          <div ref={bottomRef} />
        </div>

        {/* Sticker Picker */}
        {showStickers && (
          <div style={{ background: C.panel, borderTop: `1px solid ${C.hover}`, padding: "12px 16px" }}>
            <div style={{ fontSize: 11, color: C.text2, fontWeight: 600, marginBottom: 8, textTransform: "uppercase", letterSpacing: "0.5px" }}>Stickers</div>
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
              {STICKERS.map(s => (
                <button key={s.id} onClick={() => send({ type: "sticker", emoji: s.emoji, label: s.label, ...(replyTo ? { replyTo: getReplyPayload(replyTo) } : {}) })}
                  style={{ fontSize: 32, background: C.card, border: "none", borderRadius: 10, padding: "8px", cursor: "pointer", transition: "transform 0.1s" }}
                  title={s.label}>{s.emoji}</button>
              ))}
            </div>
          </div>
        )}

        {/* Reply Banner (WhatsApp style attached directly above composer) */}
        {replyTo && (
          <div style={{
            background: isDark ? "rgba(23, 34, 59, 0.98)" : "rgba(241, 245, 249, 0.98)",
            backdropFilter: "blur(12px)",
            WebkitBackdropFilter: "blur(12px)",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            padding: "8px 16px",
            borderTop: `1px solid ${C.hover}`,
            borderLeft: `5px solid ${C.accent}`,
            borderRadius: "14px 14px 0 0",
            margin: "0 12px -2px 12px",
            position: "relative",
            zIndex: 10,
            boxShadow: "0 -2px 10px rgba(0,0,0,0.06)",
            animation: "fadeIn 0.15s ease-out"
          }}>
            <div style={{ display: "flex", gap: 10, alignItems: "center", minWidth: 0, flex: 1 }}>
              <div style={{
                width: 28, height: 28, borderRadius: "50%",
                background: isDark ? "rgba(99, 102, 241, 0.2)" : "rgba(99, 102, 241, 0.12)",
                color: C.accent, display: "flex", alignItems: "center", justifyContent: "center",
                flexShrink: 0
              }}>
                <span style={{ fontSize: 13, fontWeight: 800 }}>↩</span>
              </div>
              <div style={{ minWidth: 0, flex: 1 }}>
                <div style={{ fontSize: 11.5, color: C.accentMid, fontWeight: 700, display: "flex", alignItems: "center", gap: 4 }}>
                  Replying to {replyTo.from === "me" ? "yourself" : (getUser(replyTo.from)?.name || "User")}
                </div>
                <div style={{ fontSize: 12.5, color: C.text2, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis", maxWidth: 360 }}>
                  {replyTo.type === "text"
                    ? replyTo.text
                    : replyTo.type === "image"
                    ? (replyTo.caption ? `📷 ${replyTo.caption}` : "📷 Photo")
                    : replyTo.type === "video"
                    ? "🎥 Video"
                    : replyTo.type === "sticker"
                    ? `Sticker ${replyTo.emoji || ""}`
                    : "Message"}
                </div>
              </div>
              {(replyTo.url || replyTo.thumb) && (
                <img src={replyTo.url || replyTo.thumb} alt="" style={{ width: 36, height: 36, borderRadius: 6, objectFit: "cover", flexShrink: 0, border: `1px solid ${C.hover}` }} />
              )}
            </div>
            <button
              onClick={() => setReplyTo(null)}
              style={{
                background: isDark ? "rgba(255,255,255,0.08)" : "rgba(0,0,0,0.05)",
                border: "none",
                color: C.text2,
                cursor: "pointer",
                width: 26,
                height: 26,
                borderRadius: "50%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                transition: "all 0.15s ease",
                flexShrink: 0,
                marginLeft: 8
              }}
              title="Cancel reply">
              <span style={{ fontSize: 12, fontWeight: 700 }}>✕</span>
            </button>
          </div>
        )}

        {/* Input Composer / Blocked Banner */}
        {isBlocked ? (
          <div style={{ background: C.panel, padding: "16px 24px", display: "flex", justifyContent: "space-between", alignItems: "center", borderTop: `1px solid ${C.hover}` }}>
            <span style={{ fontSize: 13, color: C.text2, display: "flex", alignItems: "center", gap: 8 }}>
              🚫 You have blocked {headerName}. You cannot send or receive messages.
            </span>
            <button
              onClick={() => setBlockedUsers(prev => prev.filter(id => id !== chatUser?.id))}
              style={{ background: C.card, border: `1px solid ${C.hover}`, borderRadius: 10, padding: "7px 16px", fontSize: 13, fontWeight: 700, color: C.accent, cursor: "pointer" }}>
              Unblock
            </button>
          </div>
        ) : (
          <div style={{ background: C.panel, padding: "10px 12px", display: "flex", gap: 6, alignItems: "flex-end", boxShadow: "0 -1px 1px rgba(0,0,0,0.1)" }}>
            <div style={{ background: C.bg, borderRadius: 14, padding: "4px 8px", display: "flex", gap: 4, alignItems: "center", flex: 1 }}>
              {/* Attachment Pin Symbol - Crisp & Professional */}
              <label style={{
                width: 38,
                height: 38,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "pointer",
                color: C.text2,
                borderRadius: 10,
                transition: "all 0.15s ease",
                border: "none",
                background: "transparent",
              }}
              onMouseEnter={(e) => { e.currentTarget.style.color = C.accent; e.currentTarget.style.background = C.hover; }}
              onMouseLeave={(e) => { e.currentTarget.style.color = C.text2; e.currentTarget.style.background = "transparent"; }}
              title="Attach file or media">
                <Ic.attach />
                <input type="file" accept="image/*,video/*" style={{ display: "none" }} onChange={handleMediaUpload} />
              </label>
              {/* Emoji/sticker */}
              <button onClick={() => setShowStickers(!showStickers)}
                style={{ width: 38, height: 38, background: showStickers ? C.hover : "none", border: "none", color: showStickers ? C.accent : C.text2, cursor: "pointer", borderRadius: 10, display: "flex", alignItems: "center", justifyContent: "center" }}>
                <Ic.emoji />
              </button>
              {/* Text input */}
              <textarea
                ref={inputRef}
                value={text} onChange={e => setText(e.target.value)}
                onKeyDown={e => { if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); sendText(); } }}
                placeholder={replyTo ? `Replying to ${replyTo.from === 'me' ? 'yourself' : (getUser(replyTo.from)?.name || 'message')}...` : `Type a message to ${headerName}...`}
                rows={1}
                style={{ flex: 1, background: "none", border: "none", color: C.text1, fontSize: 14, outline: "none", resize: "none", fontFamily: "'Inter', sans-serif", padding: "9px 8px", lineHeight: 1.6, maxHeight: 96, overflowY: "auto" }}
              />
            </div>
            {/* Send */}
            <button onClick={sendText}
              style={{ width: 40, height: 40, background: C.accent, border: "none", borderRadius: 12, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer", color: "#fff", boxShadow: "0 4px 8px rgba(128,131,255,0.3)", flexShrink: 0 }}>
              <Ic.send />
            </button>
          </div>
        )}
      </div>

      {/* Right Info Panel */}
      {showInfo && !isMobile && (
        <div style={{ width: 300, background: C.sidebar, borderLeft: `1px solid ${C.hover}`, display: "flex", flexDirection: "column", overflowY: "auto", flexShrink: 0 }}>
          {/* Profile header */}
          <div style={{ background: C.panel, padding: "24px 16px", textAlign: "center", borderBottom: `1px solid ${C.hover}` }}>
            <div style={{ display: "flex", justifyContent: "center", marginBottom: 12 }}>
              {isGroup
                ? <div style={{ width: 80, height: 80, borderRadius: "50%", background: chat.color, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 26, fontWeight: 700, color: C.text3 }}>{chat.initials}</div>
                : <Avatar user={chatUser} size={80} />
              }
            </div>
            <div style={{ fontWeight: 800, fontSize: 18, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>{headerName}</div>
            {!isGroup && <div style={{ fontSize: 13, color: C.accentMid, marginTop: 2 }}>{chatUser?.username}</div>}
            {!isGroup && chatUser?.bio && <div style={{ fontSize: 13, color: C.text2, marginTop: 8, lineHeight: 1.5 }}>{chatUser.bio}</div>}
            {!isGroup && (
              <div style={{ display: "flex", gap: 6, justifyContent: "center", marginTop: 12, flexWrap: "wrap", alignItems: "center" }}>
                <span style={{ background: chatUser?.isContact ? "rgba(34, 197, 94, 0.12)" : C.card, color: chatUser?.isContact ? "#16a34a" : C.text2, borderRadius: 99, padding: "3px 10px", fontSize: 11, fontWeight: 700, border: chatUser?.isContact ? "1px solid rgba(34, 197, 94, 0.3)" : `1px solid ${C.hover}` }}>
                  {chatUser?.isContact ? "✓ In Contacts" : "Not in Contacts"}
                </span>
                {isBlocked && (
                  <span style={{ background: "rgba(239, 68, 68, 0.12)", color: C.danger, borderRadius: 99, padding: "3px 10px", fontSize: 11, fontWeight: 700, border: "1px solid rgba(239, 68, 68, 0.3)" }}>
                    Blocked
                  </span>
                )}
              </div>
            )}
            {!isGroup && (
              <div style={{ fontSize: 12, color: C.text2, marginTop: 8, padding: "4px 8px", background: C.card, borderRadius: 8, display: "inline-block" }}>
                {isBlocked ? (
                  <span>🔒 Contact blocked</span>
                ) : lastSeenPrivacy === "Nobody" ? (
                  <span>🔒 Last seen hidden</span>
                ) : lastSeenPrivacy === "My Contacts" ? (
                  chatUser?.isContact 
                    ? <span>🕒 {chatUser.online ? "Online Active now" : `Last seen ${chatUser.lastSeen}`}</span>
                    : <span>🔒 Last seen hidden (Not in contacts)</span>
                ) : (
                  <span>🕒 {chatUser?.online ? "Online Active now" : `Last seen ${chatUser?.lastSeen}`}</span>
                )}
              </div>
            )}
            {isGroup && (
              <div style={{ fontSize: 13, color: C.text2, marginTop: 4 }}>{chat.members.length} members</div>
            )}
          </div>

          {/* Media, Links & Docs */}
          <div style={{ padding: "14px 14px 0" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
              <span style={{ fontSize: 13, fontWeight: 700, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Media, Links & Docs</span>
              <span style={{ fontSize: 12, color: C.text2, fontWeight: 600 }}>{sharedImages.length} ›</span>
            </div>
            {/* Media grid */}
            <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: 4 }}>
              {sharedImages.slice(0, 5).map((m, i) => (
                <div key={m.id} style={{ aspectRatio: "1", background: C.hover, borderRadius: 8, overflow: "hidden" }}>
                  <img src={m.url || m.thumb} alt="" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                </div>
              ))}
              {sharedImages.length > 5 && (
                <div style={{ aspectRatio: "1", background: C.hover, borderRadius: 8, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 12, color: C.text1, fontWeight: 700 }}>
                  +{sharedImages.length - 5}
                </div>
              )}
              {sharedImages.length === 0 && <div style={{ gridColumn: "1/-1", fontSize: 12, color: C.text2, padding: "8px 0" }}>No media, links, or docs yet.</div>}
            </div>
          </div>

          {/* Settings */}
          <div style={{ padding: 12, marginTop: 8 }}>
            <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, letterSpacing: "0.5px", textTransform: "uppercase", marginBottom: 8 }}>Chat Preferences</div>
            {[
              {
                icon: <Ic.bell />, label: "Mute notifications",
                right: (
                  <div onClick={() => setMuteNotifs(!muteNotifs)} style={{ width: 38, height: 22, background: muteNotifs ? C.accent : C.hover, borderRadius: 99, position: "relative", cursor: "pointer", transition: "background 0.2s" }}>
                    <div style={{ position: "absolute", top: 2, left: muteNotifs ? 18 : 2, width: 18, height: 18, background: "#fff", borderRadius: "50%", transition: "left 0.2s" }} />
                  </div>
                )
              },
              {
                icon: <span style={{ color: starredMessages.length > 0 ? "#f59e0b" : "inherit", display: "flex", alignItems: "center" }}><Ic.star /></span>,
                label: "Starred messages",
                onClick: () => setShowStarredModal(true),
                right: (
                  <span style={{ fontSize: 12, color: starredMessages.length > 0 ? C.accentMid : C.text2, display: "flex", gap: 3, alignItems: "center", fontWeight: 700 }}>
                    {starredMessages.length} <Ic.chevron />
                  </span>
                )
              },
            ].map((item, i) => (
              <div key={i} onClick={item.onClick} style={{ background: C.panel, borderRadius: 10, padding: "10px 12px", display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 4, cursor: item.onClick ? "pointer" : "default", transition: "background 0.15s" }}>
                <div style={{ display: "flex", gap: 10, alignItems: "center", color: C.text2 }}>
                  {item.icon}
                  <span style={{ fontSize: 13, color: C.text1 }}>{item.label}</span>
                </div>
                {item.right}
              </div>
            ))}
            {!isGroup && (
              <div onClick={() => setConfirmModal("block")} style={{ background: C.panel, borderRadius: 10, padding: "10px 12px", display: "flex", alignItems: "center", gap: 10, cursor: "pointer", marginTop: 4 }}>
                <Ic.block />
                <span style={{ fontSize: 13, color: C.danger }}>{isBlocked ? `Unblock ${headerName}` : `Block ${headerName}`}</span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Starred Messages Modal */}
      {showStarredModal && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 120, backdropFilter: "blur(4px)" }}>
          <div style={{ background: C.sidebar, borderRadius: 20, width: 440, maxWidth: "90vw", maxHeight: "80vh", display: "flex", flexDirection: "column", boxShadow: "0 24px 48px rgba(0,0,0,0.4)", border: `1px solid ${C.hover}`, overflow: "hidden" }}>
            {/* Header */}
            <div style={{ padding: "16px 20px", display: "flex", alignItems: "center", justifyContent: "space-between", borderBottom: `1px solid ${C.hover}`, background: C.panel }}>
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span style={{ color: "#f59e0b", display: "flex" }}><Ic.star /></span>
                <span style={{ fontWeight: 800, fontSize: 16, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
                  Starred Messages
                </span>
                <span style={{ background: C.card, color: C.accent, fontSize: 11, fontWeight: 700, padding: "2px 8px", borderRadius: 99 }}>
                  {starredMessages.length}
                </span>
              </div>
              <button onClick={() => setShowStarredModal(false)} style={{ background: "none", border: "none", color: C.text2, cursor: "pointer", fontSize: 16, padding: "4px 8px", borderRadius: 8 }}>
                ✕
              </button>
            </div>

            {/* Content List */}
            <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px", display: "flex", flexDirection: "column", gap: 8 }}>
              {starredMessages.length === 0 ? (
                <div style={{ textAlign: "center", padding: "36px 16px", color: C.text2, display: "flex", flexDirection: "column", alignItems: "center", gap: 10 }}>
                  <div style={{ fontSize: 36 }}>⭐</div>
                  <div style={{ fontSize: 15, fontWeight: 700, color: C.text1 }}>No starred messages yet</div>
                  <div style={{ fontSize: 13, maxWidth: 280, lineHeight: 1.5 }}>
                    Select messages and tap the <strong>Star</strong> option, or hover on any message to star it.
                  </div>
                </div>
              ) : (
                starredMessages.map((m) => {
                  const isMine = m.from === "me";
                  const senderUser = isMine ? CURRENT_USER : (isGroup ? getUser(m.from) : chatUser);
                  const senderName = isMine ? "You" : (senderUser?.name || "User");
                  return (
                    <div key={m.id}
                      onClick={() => {
                        setShowStarredModal(false);
                        scrollToMessage(m.id);
                      }}
                      style={{
                        background: C.card,
                        borderRadius: 14,
                        padding: "12px 14px",
                        border: `1px solid ${C.hover}`,
                        cursor: "pointer",
                        transition: "all 0.15s ease",
                        display: "flex",
                        flexDirection: "column",
                        gap: 6
                      }}>
                      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                        <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                          <Avatar user={senderUser} size={22} />
                          <span style={{ fontSize: 12.5, fontWeight: 700, color: isMine ? C.accentMid : C.text1 }}>{senderName}</span>
                          <span style={{ fontSize: 11, color: C.text2 }}>{m.time}</span>
                        </div>
                        <div style={{ display: "flex", gap: 4, alignItems: "center" }}>
                          <button
                            onClick={(e) => {
                              e.stopPropagation();
                              toggleStarMsg(m.id);
                            }}
                            style={{ background: "none", border: "none", color: "#f59e0b", cursor: "pointer", fontSize: 12, padding: "2px 6px", borderRadius: 6, display: "flex", alignItems: "center", gap: 3 }}
                            title="Unstar">
                            ★ <span style={{ fontSize: 11, color: C.text2 }}>Unstar</span>
                          </button>
                        </div>
                      </div>

                      {/* Message body preview */}
                      <div style={{ fontSize: 13.5, color: C.text1, lineHeight: 1.45 }}>
                        {m.type === "text" && m.text}
                        {m.type === "image" && (
                          <div style={{ display: "flex", gap: 8, alignItems: "center", marginTop: 2 }}>
                            <img src={m.url} alt="" style={{ width: 44, height: 44, borderRadius: 8, objectFit: "cover" }} />
                            <span style={{ fontSize: 12.5, color: C.text2 }}>{m.caption || "Photo"}</span>
                          </div>
                        )}
                        {m.type === "video" && (
                          <div style={{ display: "flex", gap: 8, alignItems: "center", marginTop: 2 }}>
                            <img src={m.thumb} alt="" style={{ width: 44, height: 44, borderRadius: 8, objectFit: "cover" }} />
                            <span style={{ fontSize: 12.5, color: C.text2 }}>Video ({m.duration})</span>
                          </div>
                        )}
                        {m.type === "sticker" && (
                          <div style={{ display: "flex", gap: 8, alignItems: "center", fontSize: 24 }}>
                            {m.emoji} <span style={{ fontSize: 12.5, color: C.text2 }}>Sticker: {m.label}</span>
                          </div>
                        )}
                      </div>
                      <div style={{ fontSize: 11, color: C.accent, fontWeight: 600, display: "flex", alignItems: "center", gap: 4, marginTop: 2 }}>
                        <span>Jump to message ›</span>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      )}

      {/* Confirmation Modals */}
      {confirmModal && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 120 }}>
          <div style={{ background: C.sidebar, borderRadius: 18, padding: 24, width: 380, boxShadow: "0 24px 48px rgba(0,0,0,0.4)", border: `1px solid ${C.hover}` }}>
            <h3 style={{ margin: "0 0 10px", fontSize: 17, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
              {confirmModal === "clear" && "Clear this chat?"}
              {confirmModal === "delete" && `Delete chat with ${headerName}?`}
              {confirmModal === "block" && `${isBlocked ? "Unblock" : "Block"} ${headerName}?`}
            </h3>
            <p style={{ margin: "0 0 20px", fontSize: 13, color: C.text2, lineHeight: 1.5 }}>
              {confirmModal === "clear" && "Are you sure you want to clear all messages in this conversation? This cannot be undone."}
              {confirmModal === "delete" && "This conversation and all its messages will be removed from your chat list."}
              {confirmModal === "block" && (isBlocked ? `You will be able to send and receive messages from ${headerName} again.` : `Blocked contacts will not be able to send messages to you.`)}
            </p>
            <div style={{ display: "flex", gap: 10, justifyContent: "flex-end" }}>
              <button onClick={() => setConfirmModal(null)}
                style={{ background: C.hover, border: "none", borderRadius: 10, padding: "9px 16px", fontSize: 13, fontWeight: 600, color: C.text1, cursor: "pointer" }}>
                Cancel
              </button>
              <button onClick={() => {
                if (confirmModal === "clear") {
                  onUpdateChat(chat.id, []);
                } else if (confirmModal === "delete") {
                  onDeleteChat?.(chat.id);
                } else if (confirmModal === "block") {
                  setBlockedUsers(prev => isBlocked ? prev.filter(id => id !== chatUser?.id) : [...prev, chatUser?.id]);
                }
                setConfirmModal(null);
              }}
                style={{ background: confirmModal === "block" && isBlocked ? C.accent : C.danger, border: "none", borderRadius: 10, padding: "9px 18px", fontSize: 13, fontWeight: 700, color: "#fff", cursor: "pointer" }}>
                {confirmModal === "clear" && "Clear Chat"}
                {confirmModal === "delete" && "Delete"}
                {confirmModal === "block" && (isBlocked ? "Unblock" : "Block")}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Forward Modal */}
      {showForward && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.6)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 100 }}>
          <div style={{ background: C.sidebar, borderRadius: 18, padding: 24, width: 360, boxShadow: "0 24px 48px rgba(0,0,0,0.5)" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
              <span style={{ fontWeight: 700, fontSize: 16, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Forward to…</span>
              <button onClick={() => setShowForward(null)} style={{ background: "none", border: "none", color: C.text2, cursor: "pointer" }}><Ic.close /></button>
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
              {chats.map(c => {
                const u = c.type === "direct" ? getUser(c.userId) : null;
                const name = c.type === "group" ? c.name : u?.name;
                return (
                  <button key={c.id} onClick={() => {
                    const now = new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
                    onUpdateChat(c.id, [...c.messages, { id: `m${Date.now()}`, from: "me", type: "text", text: showForward.text || "[media]", time: now, status: "sent", forwarded: true }]);
                    setShowForward(null);
                  }} style={{ display: "flex", gap: 10, alignItems: "center", padding: "8px 10px", background: C.card, border: "none", borderRadius: 10, cursor: "pointer", textAlign: "left" }}>
                    {c.type === "group"
                      ? <div style={{ width: 36, height: 36, borderRadius: "50%", background: c.color, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 12, fontWeight: 700, color: C.text3 }}>{c.initials}</div>
                      : <Avatar user={u} size={36} />
                    }
                    <span style={{ fontSize: 14, color: C.text1, fontWeight: 600 }}>{name}</span>
                  </button>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// ─── Chat List ────────────────────────────────────────────────────────────────
function ChatList({ chats, selected, onSelect, filter, onFilterChange }) {
  const { C } = useTheme();
  const [search, setSearch] = useState("");
  const filters = ["All", "Unread", "Groups"];

  const filtered = chats.filter(c => {
    if (filter === "Unread" && c.unread === 0) return false;
    if (filter === "Groups" && c.type !== "group") return false;
    if (search) {
      const name = c.type === "group" ? c.name : getUser(c.userId)?.name || "";
      return name.toLowerCase().includes(search.toLowerCase());
    }
    return true;
  });

  return (
    <div style={{ width: 300, background: C.sidebar, display: "flex", flexDirection: "column", flexShrink: 0, height: "100%", overflow: "hidden", borderRight: `1px solid ${C.hover}` }}>
      {/* Search & Filter */}
      <div style={{ padding: 12, display: "flex", flexDirection: "column", gap: 8, background: C.sidebar }}>
        <div style={{ position: "relative" }}>
          <div style={{ position: "absolute", left: 12, top: "50%", transform: "translateY(-50%)", color: C.text2 }}><Ic.search /></div>
          <input value={search} onChange={e => setSearch(e.target.value)}
            placeholder="Search conversations..."
            style={{ width: "100%", background: C.card, border: "none", borderRadius: 12, padding: "10px 12px 10px 36px", color: C.text1, fontSize: 13, outline: "none", boxSizing: "border-box" }} />
        </div>
        <div style={{ display: "flex", gap: 4, overflowX: "auto", paddingBottom: 2 }}>
          {filters.map(f => (
            <button key={f} onClick={() => onFilterChange(f)}
              style={{ padding: "3px 12px", borderRadius: 99, border: "none", fontSize: 12, fontWeight: 600, cursor: "pointer", whiteSpace: "nowrap", flexShrink: 0,
                background: filter === f ? C.accent : C.card, color: filter === f ? C.accentDk : C.text2 }}>
              {f}
            </button>
          ))}
        </div>
      </div>

      {/* Conversation list */}
      <div style={{ flex: 1, overflowY: "auto", padding: "0 8px 8px" }}>
        {filtered.length === 0 && (
          <div style={{ padding: "32px 16px", textAlign: "center", color: C.text2, fontSize: 13 }}>No conversations found.</div>
        )}
        {filtered.map(chat => {
          const isGroup = chat.type === "group";
          const user = !isGroup ? getUser(chat.userId) : null;
          const name = isGroup ? chat.name : user?.name;
          const isSelected = selected?.id === chat.id;
          return (
            <div key={chat.id} onClick={() => onSelect(chat)}
              style={{ display: "flex", gap: 10, alignItems: "center", padding: "10px 10px", borderRadius: 10, cursor: "pointer", marginBottom: 2, position: "relative",
                background: isSelected ? C.card : "transparent", transition: "background 0.15s" }}>
              {isSelected && <div style={{ position: "absolute", left: 0, top: 8, bottom: 8, width: 3, background: C.accent, borderRadius: "0 3px 3px 0" }} />}
              {isGroup
                ? <div style={{ width: 42, height: 42, borderRadius: "50%", background: chat.color, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 14, fontWeight: 700, color: C.text3, flexShrink: 0 }}>{chat.initials}</div>
                : <Avatar user={user} size={42} showOnline />
              }
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <span style={{ fontWeight: 700, fontSize: 14, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif", whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis", maxWidth: 140 }}>{name}</span>
                  <span style={{ fontSize: 11, color: chat.unread > 0 ? C.accentMid : C.text2, fontWeight: chat.unread > 0 ? 700 : 500, flexShrink: 0 }}>{chat.lastTime}</span>
                </div>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 2 }}>
                  <span style={{ fontSize: 12, color: C.text2, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis", maxWidth: 170 }}>{chat.lastMessage}</span>
                  {chat.unread > 0 && (
                    <div style={{ background: C.accent, color: C.accentDk, borderRadius: "50%", width: 18, height: 18, fontSize: 10, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>{chat.unread}</div>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ─── Status Page ─────────────────────────────────────────────────────────────
function StatusPage({ statuses, onAddStatus }) {
  const { C } = useTheme();
  const [viewer, setViewer] = useState(null);
  const [viewTimeLeft, setViewTimeLeft] = useState(30);
  const [studioOpen, setStudioOpen] = useState(false);
  const [allStatuses, setAllStatuses] = useState(statuses);
  const [now, setNow] = useState(Date.now());

  // 24-hour status expiration ticker (removes statuses older than 24h)
  useEffect(() => {
    const timer = setInterval(() => {
      const currentNow = Date.now();
      setNow(currentNow);

      setAllStatuses(prev => prev.filter(s => {
        if (s.createdAt) {
          const ageMs = currentNow - s.createdAt;
          return ageMs < 24 * 60 * 60 * 1000; // 24 hours active lifetime
        }
        return true;
      }));
    }, 10000);

    return () => clearInterval(timer);
  }, []);

  // 30-second viewer timer: displays status for 30s, then auto-exits
  useEffect(() => {
    if (!viewer) return;

    setViewTimeLeft(30);
    const interval = setInterval(() => {
      setViewTimeLeft(prev => {
        if (prev <= 1) {
          setViewer(null); // Auto-exit after 30 seconds
          return 30;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [viewer]);

  const openViewer = (status) => {
    setViewer(status);
    setViewTimeLeft(30);
    // Mark as viewed
    setAllStatuses(prev => prev.map(s => s.id === status.id ? { ...s, viewed: true } : s));
  };

  const myStatuses = allStatuses.filter(s => s.userId === "me");

  const handlePublishStudio = (statusData) => {
    const createdAt = Date.now();
    const ns = {
      id: `st${createdAt}`,
      userId: "me",
      time: "Just now",
      viewed: false,
      createdAt: createdAt,
      ...statusData
    };
    setAllStatuses(prev => [ns, ...prev]);
    setStudioOpen(false);
  };

  const get24hRemaining = (createdAt) => {
    if (!createdAt) return null;
    const elapsed = now - createdAt;
    const remainingMs = (24 * 60 * 60 * 1000) - elapsed;
    if (remainingMs <= 0) return "Expired";
    const hours = Math.floor(remainingMs / (60 * 60 * 1000));
    const mins = Math.floor((remainingMs % (60 * 60 * 1000)) / (60 * 1000));
    if (hours > 0) return `${hours}h left`;
    return `${mins}m left`;
  };

  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", overflow: "hidden" }}>
      {/* Top Header - Text Status and Image Status buttons removed */}
      <div style={{ background: C.panel, padding: "0 24px", height: 64, display: "flex", alignItems: "center", justifyContent: "space-between", borderBottom: `1px solid ${C.hover}` }}>
        <span style={{ fontWeight: 800, fontSize: 18, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Status</span>
        <span style={{ fontSize: 12, color: C.text2 }}>Updates expire after 24h</span>
      </div>

      <div style={{ flex: 1, overflowY: "auto", padding: 24 }}>
        {/* My status */}
        <div style={{ marginBottom: 28 }}>
          <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: 12 }}>My Status (Active for 24h)</div>
          <div style={{ display: "flex", gap: 12, flexWrap: "wrap", alignItems: "center" }}>
            {/* Plus with circle Add Status Button */}
            <div
              onClick={() => setStudioOpen(true)}
              style={{
                width: 110,
                height: 150,
                borderRadius: 14,
                background: C.panel,
                border: `2px dashed ${C.accent}`,
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                gap: 8,
                cursor: "pointer",
                padding: 10,
                boxSizing: "border-box",
                transition: "all 0.2s"
              }}
              title="Add Status"
            >
              <div style={{
                width: 44,
                height: 44,
                borderRadius: "50%",
                background: "rgba(128, 131, 255, 0.18)",
                color: C.accent,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                border: `2px solid ${C.accent}`
              }}>
                <Ic.plus />
              </div>
              <span style={{ fontSize: 11, fontWeight: 700, color: C.text1, textAlign: "center" }}>Add Status</span>
              <span style={{ fontSize: 9, color: C.text2, textAlign: "center" }}>24h Story</span>
            </div>

            {/* User's posted statuses */}
            {myStatuses.map(s => (
              <StatusTile key={s.id} status={s} onView={() => openViewer(s)} onDelete={() => setAllStatuses(prev => prev.filter(st => st.id !== s.id))} isMine remainingText={get24hRemaining(s.createdAt)} />
            ))}
          </div>
        </div>

        {/* Recent updates */}
        <div>
          <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: 12 }}>Recent Updates</div>
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            {allStatuses.filter(s => s.userId !== "me").map(s => {
              const u = getUser(s.userId);
              const remaining = get24hRemaining(s.createdAt);

              return (
                <div key={s.id} onClick={() => openViewer(s)}
                  style={{ background: C.panel, borderRadius: 14, padding: "12px 14px", display: "flex", alignItems: "center", gap: 12, cursor: "pointer", transition: "background 0.15s" }}>
                  <div style={{ position: "relative" }}>
                    <div style={{ width: 48, height: 48, borderRadius: "50%", border: `2px solid ${s.viewed ? C.hover : C.accent}`, padding: 2 }}>
                      <Avatar user={u} size={40} />
                    </div>
                  </div>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: 14, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>{u?.name}</div>
                    <div style={{ fontSize: 12, color: C.text2 }}>
                      {s.time} {remaining ? `• ${remaining}` : ""}
                    </div>
                  </div>
                  {!s.viewed && <div style={{ marginLeft: "auto", width: 8, height: 8, borderRadius: "50%", background: C.accent }} />}
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Status Viewer with 30s auto-exit timer */}
      {viewer && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,0.85)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 200 }}>
          <div style={{ width: 340, borderRadius: 20, overflow: "hidden", position: "relative", boxShadow: "0 24px 48px rgba(0,0,0,0.6)" }}>
            {/* Top Close Button */}
            <div style={{ position: "absolute", top: 12, right: 12, display: "flex", justifyContent: "flex-end", alignItems: "center", zIndex: 12 }}>
              <button onClick={() => setViewer(null)} style={{ background: "rgba(0,0,0,0.55)", border: "none", color: "#fff", cursor: "pointer", borderRadius: "50%", width: 32, height: 32, display: "flex", alignItems: "center", justifyContent: "center" }}><Ic.close /></button>
            </div>

            {/* 30-second Animated Progress Bar */}
            <div style={{ position: "absolute", top: 0, left: 0, right: 0, height: 4, background: "rgba(255,255,255,0.25)", zIndex: 11 }}>
              <div style={{
                height: "100%",
                width: `${((30 - viewTimeLeft) / 30) * 100}%`,
                background: C.accent,
                borderRadius: 3,
                transition: "width 1s linear"
              }} />
            </div>

            {viewer.url ? (
              <div style={{ width: "100%", height: 500, overflow: "hidden", position: "relative", display: "flex", alignItems: "center", justifyContent: "center", background: viewer.bg || "#000" }}>
                <img
                  src={viewer.url}
                  alt=""
                  style={{
                    width: "100%",
                    height: "100%",
                    objectFit: viewer.aspectRatio === "1:1" ? "contain" : "cover",
                    transform: `rotate(${viewer.rotation || 0}deg) scale(${viewer.cropZoom || 1})`,
                    transition: "transform 0.2s"
                  }}
                />
                {viewer.text && (
                  <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", padding: 24, background: "rgba(0,0,0,0.3)" }}>
                    <p style={{ color: viewer.textColor || "#fff", fontSize: 20, fontWeight: 700, textAlign: "center", fontFamily: "'Plus Jakarta Sans', sans-serif", textShadow: "0 2px 8px rgba(0,0,0,0.85)", lineHeight: 1.4 }}>
                      {viewer.text}
                    </p>
                  </div>
                )}
              </div>
            ) : (
              <div style={{ height: 500, background: viewer.bg || C.accent, display: "flex", alignItems: "center", justifyContent: "center", padding: 32 }}>
                <p style={{ color: viewer.textColor || "#fff", fontSize: 22, fontWeight: 700, textAlign: "center", fontFamily: "'Plus Jakarta Sans', sans-serif", lineHeight: 1.4 }}>
                  {viewer.text}
                </p>
              </div>
            )}

            <div style={{ position: "absolute", bottom: 0, left: 0, right: 0, background: "linear-gradient(transparent, rgba(0,0,0,0.75))", padding: "24px 16px 16px", display: "flex", gap: 10, alignItems: "center" }}>
              <Avatar user={getUser(viewer.userId) || CURRENT_USER} size={36} />
              <div>
                <div style={{ fontSize: 14, fontWeight: 700, color: "#fff" }}>{viewer.userId === "me" ? "You" : getUser(viewer.userId)?.name}</div>
                <div style={{ fontSize: 12, color: "rgba(255,255,255,0.7)" }}>
                  {viewer.time}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Blank Page Status Studio Modal (Text + Emojis + Image + Crop & Rotate) */}
      {studioOpen && (
        <StatusStudioModal
          onClose={() => setStudioOpen(false)}
          onPublish={handlePublishStudio}
        />
      )}
    </div>
  );
}

function StatusStudioModal({ onClose, onPublish }) {
  const { C } = useTheme();
  const [text, setText] = useState("");
  const [image, setImage] = useState(null);
  const [rotation, setRotation] = useState(0);
  const [aspectRatio, setAspectRatio] = useState("9:16");
  const [cropZoom, setCropZoom] = useState(1);
  const [bgColor, setBgColor] = useState("#8083ff");
  const [textColor, setTextColor] = useState("#ffffff");
  const [activeTab, setActiveTab] = useState("text"); // "text" | "image" | "crop"

  const colors = ["#8083ff", "#3626ce", "#0b1326", "#c3c0ff", "#171f33", "#222a3d", "#e11d48", "#059669", "#d97706", "#7c3aed"];
  
  // Real emojis list (strictly emojis, not stickers)
  const EMOJIS = ["😊", "🎉", "🔥", "💜", "🚀", "😂", "👏", "✨", "🌟", "🎯", "❤️", "👍", "🥳", "😍", "😎", "💯", "🙌", "⚡️", "💖", "🤩", "🌸", "🍕", "💎", "🥂", "🎂", "💪", "💡", "🌈", "🕊️", "☕", "🏖️", "✈️", "🎵", "🏆", "🎁", "💥", "🎈"];

  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const url = URL.createObjectURL(file);
    setImage(url);
    setActiveTab("crop");
  };

  const rotateImage = () => {
    setRotation(prev => (prev + 90) % 360);
  };

  const addEmoji = (emoji) => {
    setText(prev => prev + emoji);
  };

  const handlePublish = () => {
    if (!text.trim() && !image) return;
    onPublish({
      type: image ? "image" : "text",
      text: text.trim(),
      url: image,
      rotation,
      aspectRatio,
      cropZoom,
      bg: bgColor,
      textColor: textColor
    });
  };

  return (
    <div style={{ position: "fixed", inset: 0, background: C.modalOverlay, display: "flex", alignItems: "center", justifyContent: "center", zIndex: 220, padding: 16 }}>
      <div style={{ width: 440, maxHeight: "92vh", background: C.sidebar, borderRadius: 24, overflow: "hidden", display: "flex", flexDirection: "column", boxShadow: "0 24px 48px rgba(0,0,0,0.15)", border: `1px solid ${C.hover}` }}>
        
        {/* Studio Header */}
        <div style={{ padding: "14px 20px", display: "flex", justifyContent: "space-between", alignItems: "center", borderBottom: `1px solid ${C.hover}`, background: C.panel }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <span style={{ fontWeight: 800, fontSize: 16, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Create Status</span>
            <span style={{ background: C.accentLt, color: C.accentMid, fontSize: 10, fontWeight: 700, padding: "2px 8px", borderRadius: 99 }}>⏱️ 24h Story</span>
          </div>
          <button onClick={onClose} style={{ background: "none", border: "none", color: C.text2, cursor: "pointer", fontSize: 16 }}><Ic.close /></button>
        </div>

        {/* Live Blank Canvas Preview */}
        <div style={{ padding: "16px 20px 8px", display: "flex", justifyContent: "center", background: C.bg }}>
          <div style={{
            width: 240,
            height: 280,
            borderRadius: 16,
            background: image ? "#000" : bgColor,
            overflow: "hidden",
            position: "relative",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            boxShadow: "0 8px 24px rgba(0,0,0,0.12)",
            border: `1px solid ${C.hover}`
          }}>
            {/* Image Layer with Rotation & Crop */}
            {image ? (
              <img
                src={image}
                alt="Status preview"
                style={{
                  width: "100%",
                  height: "100%",
                  objectFit: aspectRatio === "1:1" ? "contain" : "cover",
                  transform: `rotate(${rotation}deg) scale(${cropZoom})`,
                  transition: "transform 0.2s"
                }}
              />
            ) : null}

            {/* Overlaid Text + Emojis Layer */}
            <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", padding: 16, background: image ? "rgba(0,0,0,0.3)" : "none" }}>
              <p style={{
                color: textColor,
                fontSize: 16,
                fontWeight: 700,
                textAlign: "center",
                margin: 0,
                wordBreak: "break-word",
                fontFamily: "'Plus Jakarta Sans', sans-serif",
                textShadow: image ? "0 2px 6px rgba(0,0,0,0.9)" : "none",
                lineHeight: 1.4
              }}>
                {text || (image ? "" : "Tap below to add text & emojis...")}
              </p>
            </div>
          </div>
        </div>

        {/* Studio Tool Tabs */}
        <div style={{ display: "flex", borderBottom: `1px solid ${C.hover}`, background: C.panel }}>
          {[
            { id: "text", label: "✍️ Text & Emojis" },
            { id: "image", label: "🖼️ Image" },
            { id: "crop", label: "🔄 Crop & Rotate" },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                flex: 1,
                padding: "10px 4px",
                background: "none",
                border: "none",
                borderBottom: activeTab === tab.id ? `2px solid ${C.accent}` : "2px solid transparent",
                color: activeTab === tab.id ? C.accent : C.text2,
                fontSize: 12,
                fontWeight: 700,
                cursor: "pointer"
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Tab Content Controls */}
        <div style={{ padding: "16px 20px", flex: 1, overflowY: "auto", maxHeight: 220 }}>
          
          {/* TAB 1: Text & Emojis */}
          {activeTab === "text" && (
            <div>
              <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, marginBottom: 6 }}>Status Message</div>
              <textarea
                value={text}
                onChange={e => setText(e.target.value)}
                placeholder="Type your message with emojis... 😊"
                rows={2}
                style={{
                  width: "100%",
                  background: C.card,
                  border: `1px solid ${C.hover}`,
                  borderRadius: 12,
                  padding: "10px 12px",
                  color: C.text1,
                  fontSize: 14,
                  resize: "none",
                  outline: "none",
                  boxSizing: "border-box",
                  marginBottom: 10,
                  fontFamily: "'Plus Jakarta Sans', sans-serif"
                }}
              />

              {/* Emoji Picker Bar */}
              <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, marginBottom: 6 }}>Add Emojis</div>
              <div style={{ display: "flex", gap: 6, overflowX: "auto", paddingBottom: 6, marginBottom: 12, scrollbarWidth: "thin" }}>
                {EMOJIS.map((emoji, idx) => (
                  <button
                    key={idx}
                    onClick={() => addEmoji(emoji)}
                    style={{ background: C.card, border: "none", borderRadius: 8, padding: "6px 8px", fontSize: 17, cursor: "pointer", flexShrink: 0 }}
                  >
                    {emoji}
                  </button>
                ))}
              </div>

              {/* Background Color Palette */}
              {!image && (
                <div>
                  <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, marginBottom: 6 }}>Background Color</div>
                  <div style={{ display: "flex", gap: 6 }}>
                    {colors.map(c => (
                      <button
                        key={c}
                        onClick={() => setBgColor(c)}
                        style={{ width: 24, height: 24, borderRadius: "50%", background: c, border: bgColor === c ? `2px solid #fff` : "2px solid transparent", cursor: "pointer" }}
                      />
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* TAB 2: Image Tool */}
          {activeTab === "image" && (
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              <label style={{
                background: C.card,
                border: `1px dashed ${C.accent}`,
                borderRadius: 12,
                padding: "14px",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: 8,
                cursor: "pointer",
                color: C.text1,
                fontWeight: 700,
                fontSize: 13
              }}>
                <Ic.camera />
                {image ? "Replace Image" : "Upload Image"}
                <input type="file" accept="image/*" style={{ display: "none" }} onChange={handleImageUpload} />
              </label>

              {image && (
                <button
                  onClick={() => { setImage(null); setRotation(0); setCropZoom(1); }}
                  style={{ background: "#fee2e2", border: "1px solid #fca5a5", borderRadius: 10, padding: "8px", color: C.danger, fontSize: 12, fontWeight: 700, cursor: "pointer" }}
                >
                  Remove Image
                </button>
              )}
            </div>
          )}

          {/* TAB 3: Crop & Rotate */}
          {activeTab === "crop" && (
            <div>
              {/* Rotate Button */}
              <div style={{ marginBottom: 12 }}>
                <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, marginBottom: 6 }}>Rotate</div>
                <button
                  onClick={rotateImage}
                  style={{
                    background: C.card,
                    border: `1px solid ${C.hover}`,
                    borderRadius: 10,
                    padding: "8px 14px",
                    color: C.text1,
                    fontSize: 13,
                    fontWeight: 700,
                    cursor: "pointer",
                    display: "flex",
                    alignItems: "center",
                    gap: 8
                  }}
                >
                  🔄 Rotate 90° ({rotation}°)
                </button>
              </div>

              {/* Crop Aspect Ratio */}
              <div style={{ marginBottom: 12 }}>
                <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, marginBottom: 6 }}>Crop / Aspect Ratio</div>
                <div style={{ display: "flex", gap: 6 }}>
                  {[
                    { id: "9:16", label: "📱 Story (9:16)" },
                    { id: "1:1", label: "🔲 Square (1:1)" },
                    { id: "4:5", label: "🖼️ Portrait (4:5)" },
                  ].map(ratio => (
                    <button
                      key={ratio.id}
                      onClick={() => setAspectRatio(ratio.id)}
                      style={{
                        flex: 1,
                        padding: "6px 8px",
                        borderRadius: 8,
                        border: "none",
                        fontSize: 11,
                        fontWeight: 700,
                        cursor: "pointer",
                        background: aspectRatio === ratio.id ? C.accent : C.card,
                        color: aspectRatio === ratio.id ? "#fff" : C.text2
                      }}
                    >
                      {ratio.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Crop Zoom */}
              <div>
                <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, marginBottom: 6 }}>Zoom / Scale</div>
                <div style={{ display: "flex", gap: 6 }}>
                  {[1, 1.25, 1.5].map(z => (
                    <button
                      key={z}
                      onClick={() => setCropZoom(z)}
                      style={{
                        flex: 1,
                        padding: "6px",
                        borderRadius: 8,
                        border: "none",
                        fontSize: 12,
                        fontWeight: 700,
                        cursor: "pointer",
                        background: cropZoom === z ? C.accent : C.card,
                        color: cropZoom === z ? "#fff" : C.text2
                      }}
                    >
                      {z}x
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

        </div>

        {/* Studio Footer */}
        <div style={{ padding: "12px 20px", display: "flex", gap: 10, background: C.panel, borderTop: `1px solid ${C.hover}` }}>
          <button onClick={onClose} style={{ flex: 1, background: C.card, border: "none", borderRadius: 12, padding: "10px", color: C.text2, fontSize: 13, fontWeight: 600, cursor: "pointer" }}>Cancel</button>
          <button onClick={handlePublish} style={{ flex: 2, background: C.accent, border: "none", borderRadius: 12, padding: "10px", color: "#fff", fontSize: 14, fontWeight: 700, cursor: "pointer", boxShadow: "0 4px 12px rgba(99,102,241,0.25)" }}>
            Publish Status
          </button>
        </div>

      </div>
    </div>
  );
}

function StatusTile({ status, onView, onDelete, isMine, remainingText }) {
  const { C } = useTheme();
  return (
    <div style={{ position: "relative", borderRadius: 14, overflow: "hidden", width: 110, height: 150, cursor: "pointer", background: status.bg || C.panel, boxShadow: "0 2px 8px rgba(0,0,0,0.08)", border: `1px solid ${C.hover}` }} onClick={onView}>
      {status.url ? (
        <div style={{ width: "100%", height: "100%", overflow: "hidden", position: "relative" }}>
          <img
            src={status.url}
            alt=""
            style={{
              width: "100%",
              height: "100%",
              objectFit: "cover",
              transform: `rotate(${status.rotation || 0}deg) scale(${status.cropZoom || 1})`,
              transition: "transform 0.2s"
            }}
          />
          {status.text && (
            <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", padding: 6, background: "rgba(0,0,0,0.35)" }}>
              <p style={{ color: "#fff", fontSize: 11, fontWeight: 700, textAlign: "center", margin: 0, wordBreak: "break-word", textShadow: "0 1px 3px rgba(0,0,0,0.8)" }}>{status.text}</p>
            </div>
          )}
        </div>
      ) : (
        <div style={{ width: "100%", height: "100%", background: status.bg || C.accent, display: "flex", alignItems: "center", justifyContent: "center", padding: 8 }}>
          <p style={{ color: status.textColor || "#fff", fontSize: 12, fontWeight: 700, textAlign: "center", margin: 0, wordBreak: "break-word" }}>{status.text}</p>
        </div>
      )}
      {isMine && (
        <button onClick={e => { e.stopPropagation(); onDelete(); }}
          style={{ position: "absolute", top: 6, right: 6, background: "rgba(0,0,0,0.65)", border: "none", color: "#fff", borderRadius: "50%", width: 22, height: 22, cursor: "pointer", fontSize: 10, zIndex: 5, display: "flex", alignItems: "center", justifyContent: "center" }}>✕</button>
      )}
      <div style={{ position: "absolute", bottom: 0, left: 0, right: 0, background: "linear-gradient(transparent, rgba(0,0,0,0.8))", padding: "8px 6px 6px", fontSize: 10, color: "#fff", fontWeight: 700, display: "flex", justifyContent: "space-between", alignItems: "center", zIndex: 4 }}>
        <span>{status.time}</span>
        {remainingText && <span style={{ background: "rgba(255,255,255,0.2)", padding: "1px 4px", borderRadius: 4, fontSize: 9 }}>{remainingText}</span>}
      </div>
    </div>
  );
}

// ─── Profile Page ─────────────────────────────────────────────────────────────
function ProfilePage() {
  const { C, userProfile, setUserProfile } = useTheme();
  const [name, setName] = useState(userProfile?.name || "You");
  const [bio, setBio] = useState(userProfile?.bio || "");
  const [username, setUsername] = useState(userProfile?.username || "@you");
  const [avatar, setAvatar] = useState(userProfile?.avatar || null);
  const [saved, setSaved] = useState(false);

  // Sync state whenever userProfile changes
  useEffect(() => {
    setName(userProfile?.name || "You");
    setBio(userProfile?.bio || "");
    setUsername(userProfile?.username || "@you");
    setAvatar(userProfile?.avatar || null);
  }, [userProfile]);

  const handleAvatarUpload = (e) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        const dataUrl = event.target.result;
        setAvatar(dataUrl);
        setUserProfile(prev => ({
          ...prev,
          name: name.trim() || "You",
          username: username.trim().startsWith("@") ? username.trim() : `@${username.trim()}`,
          bio: bio,
          avatar: dataUrl
        }));
        setSaved(true);
        setTimeout(() => setSaved(false), 2500);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleRemoveAvatar = () => {
    setAvatar(null);
    setUserProfile(prev => ({
      ...prev,
      name: name.trim() || "You",
      username: username.trim().startsWith("@") ? username.trim() : `@${username.trim()}`,
      bio: bio,
      avatar: null
    }));
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  const save = () => {
    const formattedUsername = username.trim().startsWith("@") ? username.trim() : `@${username.trim()}`;
    const formattedName = name.trim() || "You";
    setUserProfile({
      name: formattedName,
      username: formattedUsername,
      bio: bio,
      avatar: avatar,
    });
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  const initials = (name.trim() || "You")
    .split(" ")
    .map(w => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase() || "YO";

  return (
    <div style={{ flex: 1, overflowY: "auto" }}>
      <div style={{ background: C.panel, height: 64, display: "flex", alignItems: "center", padding: "0 24px", borderBottom: `1px solid ${C.hover}` }}>
        <span style={{ fontWeight: 800, fontSize: 18, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Profile</span>
      </div>
      <div style={{ maxWidth: 520, margin: "32px auto", padding: "0 24px" }}>
        {/* Avatar */}
        <div style={{ textAlign: "center", marginBottom: 32 }}>
          <div style={{ position: "relative", display: "inline-block" }}>
            {avatar ? (
              <img
                src={avatar}
                alt={name}
                style={{ width: 100, height: 100, borderRadius: "50%", objectFit: "cover", border: `3px solid ${C.accent}`, display: "block" }}
              />
            ) : (
              <div style={{ width: 100, height: 100, borderRadius: "50%", background: C.groupBg, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 34, fontWeight: 800, color: "#fff", fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
                {initials}
              </div>
            )}
            <label title="Change Photo" style={{ position: "absolute", bottom: 2, right: 2, background: C.accent, borderRadius: "50%", width: 32, height: 32, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer", border: `2px solid ${C.sidebar}`, color: "#fff", boxShadow: "0 2px 6px rgba(0,0,0,0.25)" }}>
              <Ic.camera />
              <input type="file" accept="image/*" onChange={handleAvatarUpload} style={{ display: "none" }} />
            </label>
          </div>
          {avatar && (
            <div style={{ marginTop: 8 }}>
              <button
                type="button"
                onClick={handleRemoveAvatar}
                style={{ background: "none", border: "none", color: C.danger, fontSize: 12, fontWeight: 600, cursor: "pointer", textDecoration: "underline" }}
              >
                Remove Photo
              </button>
            </div>
          )}
        </div>

        {[
          { label: "Display Name", val: name, set: setName, placeholder: "Your name" },
          { label: "Username", val: username, set: setUsername, placeholder: "@username" },
        ].map(f => (
          <div key={f.label} style={{ marginBottom: 18 }}>
            <label style={{ display: "block", fontSize: 12, color: C.text2, fontWeight: 700, marginBottom: 6 }}>{f.label}</label>
            <input value={f.val} onChange={e => f.set(e.target.value)} placeholder={f.placeholder}
              style={{ width: "100%", background: C.card, border: `1px solid ${C.hover}`, borderRadius: 12, padding: "11px 14px", color: C.text1, fontSize: 14, boxSizing: "border-box", outline: "none" }} />
          </div>
        ))}
        <div style={{ marginBottom: 24 }}>
          <label style={{ display: "block", fontSize: 12, color: C.text2, fontWeight: 700, marginBottom: 6 }}>Bio</label>
          <textarea value={bio} onChange={e => setBio(e.target.value)} rows={3}
            style={{ width: "100%", background: C.card, border: `1px solid ${C.hover}`, borderRadius: 12, padding: "11px 14px", color: C.text1, fontSize: 14, boxSizing: "border-box", outline: "none", resize: "none", fontFamily: "'Inter', sans-serif" }} />
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <button onClick={save} style={{ background: saved ? "#22c55e" : C.accent, border: "none", borderRadius: 12, padding: "12px 28px", color: "#fff", fontSize: 15, fontWeight: 700, cursor: "pointer", transition: "background 0.3s", boxShadow: "0 4px 12px rgba(99,102,241,0.25)" }}>
            {saved ? "Saved ✓" : "Save Changes"}
          </button>
          {saved && (
            <span style={{ fontSize: 13, color: "#22c55e", fontWeight: 700 }}>
              Profile details updated!
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── Connections Page ─────────────────────────────────────────────────────────
function ConnectionsPage({
  connections = [],
  onSendRequest,
  onAcceptRequest,
  onRejectRequest,
  onMessageUser
}) {
  const { C, theme, userProfile } = useTheme();
  const [searchEmail, setSearchEmail] = useState("");
  const [searched, setSearched] = useState(false);
  const [searchResult, setSearchResult] = useState(null); // null | user | "not_found" | "self" | "invalid_email"
  const [toast, setToast] = useState("");

  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(""), 3500);
  };

  const validateEmail = (email) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
  };

  const handleSearch = (e) => {
    if (e) e.preventDefault();
    const query = searchEmail.trim().toLowerCase();
    setSearched(true);

    if (!query || !validateEmail(query)) {
      setSearchResult("invalid_email");
      return;
    }

    // Check if self email
    const myEmail = (userProfile?.email || "hello@example.com").toLowerCase();
    const myUserEmail = (userProfile?.username ? userProfile.username.replace("@", "") + "@example.com" : "").toLowerCase();
    if (query === myEmail || query === myUserEmail) {
      setSearchResult("self");
      return;
    }

    // Search in USERS
    const foundUser = USERS.find(u => u.email?.toLowerCase() === query || (u.username && u.username.toLowerCase().replace("@", "") + "@example.com" === query));
    if (foundUser) {
      setSearchResult(foundUser);
    } else {
      setSearchResult("not_found");
    }
  };

  const handleQuickSearch = (email) => {
    setSearchEmail(email);
    const query = email.trim().toLowerCase();
    setSearched(true);
    const foundUser = USERS.find(u => u.email?.toLowerCase() === query);
    if (foundUser) {
      setSearchResult(foundUser);
    } else {
      setSearchResult("not_found");
    }
  };

  const getConnectionStatus = (userId) => {
    const conn = connections.find(c => c.userId === userId);
    return conn ? conn.status : "none"; // "connected" | "incoming" | "sent" | "rejected" | "none"
  };

  const incomingRequests = connections
    .filter(c => c.status === "incoming")
    .map(c => ({ ...c, user: getUser(c.userId) }))
    .filter(c => c.user);

  const connectedFriends = connections
    .filter(c => c.status === "connected")
    .map(c => ({ ...c, user: getUser(c.userId) }))
    .filter(c => c.user);

  return (
    <div style={{ flex: 1, overflowY: "auto", background: C.bg }}>
      {/* Header */}
      <div style={{ background: C.panel, height: 64, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 24px", borderBottom: `1px solid ${C.hover}` }}>
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          <span style={{ fontWeight: 800, fontSize: 18, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Connections</span>
          <span style={{ fontSize: 12, color: C.text2, background: C.card, padding: "3px 8px", borderRadius: 99, fontWeight: 700 }}>
            {connectedFriends.length} {connectedFriends.length === 1 ? "friend" : "friends"}
          </span>
        </div>
        <span style={{ fontSize: 12, color: C.text2 }}>Find and connect with your friends</span>
      </div>

      <div style={{ maxWidth: 680, margin: "24px auto", padding: "0 20px" }}>
        {/* Toast Alert */}
        {toast && (
          <div style={{
            background: "rgba(34, 197, 94, 0.15)",
            color: "#22c55e",
            borderRadius: 12,
            padding: "12px 18px",
            marginBottom: 20,
            border: "1.5px solid #22c55e",
            fontSize: 14,
            fontWeight: 700,
            display: "flex",
            alignItems: "center",
            gap: 8,
            boxShadow: "0 4px 12px rgba(34, 197, 94, 0.15)"
          }}>
            <span>✓</span> {toast}
          </div>
        )}

        {/* ─── Search by Email Section ─── */}
        <div style={{ background: C.panel, borderRadius: 16, padding: "24px", border: `1px solid ${C.hover}`, marginBottom: 28, boxShadow: "0 4px 16px rgba(0,0,0,0.03)" }}>
          <div style={{ marginBottom: 16 }}>
            <div style={{ fontSize: 16, fontWeight: 800, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif", marginBottom: 4 }}>
              Find Friend Using Email
            </div>
            <div style={{ fontSize: 13, color: C.text2 }}>
              Enter another user's email address to send them a connection request.
            </div>
          </div>

          <form onSubmit={handleSearch} style={{ display: "flex", gap: 10, flexWrap: "wrap", marginBottom: 14 }}>
            <div style={{ position: "relative", flex: "1 1 260px" }}>
              <span style={{ position: "absolute", left: 14, top: "50%", transform: "translateY(-50%)", color: C.text2, display: "flex", alignItems: "center" }}>
                <Ic.search />
              </span>
              <input
                type="email"
                value={searchEmail}
                onChange={e => { setSearchEmail(e.target.value); setSearched(false); }}
                placeholder="Enter friend's email address (e.g. alex@example.com)"
                style={{
                  width: "100%",
                  background: C.card,
                  border: `1px solid ${C.hover}`,
                  borderRadius: 12,
                  padding: "12px 14px 12px 38px",
                  color: C.text1,
                  fontSize: 14,
                  boxSizing: "border-box",
                  outline: "none"
                }}
              />
            </div>
            <button
              type="submit"
              style={{
                background: C.accent,
                color: "#fff",
                border: "none",
                borderRadius: 12,
                padding: "12px 24px",
                fontSize: 14,
                fontWeight: 700,
                cursor: "pointer",
                boxShadow: "0 4px 12px rgba(99,102,241,0.25)",
                display: "flex",
                alignItems: "center",
                gap: 6
              }}
            >
              Find User
            </button>
          </form>

          {/* Suggested Quick Test Chips */}
          <div style={{ display: "flex", alignItems: "center", gap: 6, flexWrap: "wrap", fontSize: 12, color: C.text2 }}>
            <span style={{ fontWeight: 600 }}>Quick suggestions:</span>
            {USERS.map(u => (
              <button
                key={u.id}
                type="button"
                onClick={() => handleQuickSearch(u.email)}
                style={{
                  background: C.card,
                  border: `1px solid ${C.hover}`,
                  borderRadius: 8,
                  padding: "3px 8px",
                  color: C.text1,
                  fontSize: 11,
                  cursor: "pointer",
                  transition: "all 0.15s"
                }}
              >
                {u.email}
              </button>
            ))}
          </div>

          {/* Search Result States */}
          {searched && (
            <div style={{ marginTop: 20, borderTop: `1px solid ${C.hover}`, paddingTop: 18 }}>
              {/* Invalid Email */}
              {searchResult === "invalid_email" && (
                <div style={{ background: "rgba(239, 68, 68, 0.1)", color: "#ef4444", padding: "12px 16px", borderRadius: 12, fontSize: 13, fontWeight: 600, border: "1px solid #ef4444" }}>
                  Please enter a valid email address.
                </div>
              )}

              {/* Self Search */}
              {searchResult === "self" && (
                <div style={{ background: "rgba(234, 179, 8, 0.12)", color: "#eab308", padding: "14px 16px", borderRadius: 12, fontSize: 13, fontWeight: 700, border: "1px solid #eab308", display: "flex", alignItems: "center", gap: 8 }}>
                  <span>⚠️</span> You cannot send a connection request to yourself.
                </div>
              )}

              {/* Not Found */}
              {searchResult === "not_found" && (
                <div style={{ textAlign: "center", padding: "20px 10px" }}>
                  <div style={{ fontSize: 32, marginBottom: 8 }}>🔍</div>
                  <div style={{ fontSize: 15, fontWeight: 700, color: C.text1, marginBottom: 4 }}>No user found</div>
                  <div style={{ fontSize: 13, color: C.text2, maxWidth: 360, margin: "0 auto" }}>
                    We couldn't find an account with this email address. Please check the email and try again.
                  </div>
                </div>
              )}

              {/* User Found Card */}
              {searchResult && typeof searchResult === "object" && (() => {
                const status = getConnectionStatus(searchResult.id);
                return (
                  <div>
                    <div style={{ fontSize: 11, fontWeight: 700, color: C.text2, textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: 10 }}>
                      Search Result
                    </div>
                    <div style={{
                      background: C.card,
                      borderRadius: 14,
                      padding: "16px 18px",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "space-between",
                      gap: 14,
                      flexWrap: "wrap",
                      border: `1px solid ${C.hover}`
                    }}>
                      <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
                        <Avatar user={searchResult} size={48} showOnline />
                        <div>
                          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <span style={{ fontSize: 15, fontWeight: 700, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
                              {searchResult.name}
                            </span>
                            <span style={{ fontSize: 12, color: C.accent, fontWeight: 600 }}>
                              {searchResult.username}
                            </span>
                          </div>
                          <div style={{ fontSize: 12, color: C.text2, marginTop: 2 }}>
                            {searchResult.email}
                          </div>
                          {searchResult.bio && (
                            <div style={{ fontSize: 12, color: C.text2, marginTop: 4, fontStyle: "italic", maxWidth: 320 }}>
                              "{searchResult.bio}"
                            </div>
                          )}
                        </div>
                      </div>

                      {/* Action buttons based on status */}
                      <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                        {status === "connected" && (
                          <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                            <span style={{ background: "rgba(34, 197, 94, 0.15)", color: "#22c55e", padding: "6px 12px", borderRadius: 8, fontSize: 12, fontWeight: 700 }}>
                              ✓ Connected
                            </span>
                            <button
                              type="button"
                              onClick={() => onMessageUser(searchResult)}
                              style={{
                                background: C.accent,
                                color: "#fff",
                                border: "none",
                                borderRadius: 10,
                                padding: "8px 16px",
                                fontSize: 13,
                                fontWeight: 700,
                                cursor: "pointer",
                                boxShadow: "0 3px 10px rgba(99,102,241,0.25)"
                              }}
                            >
                              Message
                            </button>
                          </div>
                        )}

                        {status === "sent" && (
                          <div style={{ textAlign: "right" }}>
                            <span style={{ background: (theme === "dark" ? "rgba(128,131,255,0.2)" : "rgba(99,102,241,0.1)"), color: C.accent, padding: "6px 12px", borderRadius: 8, fontSize: 12, fontWeight: 700 }}>
                              ✓ Request Sent
                            </span>
                            <div style={{ fontSize: 11, color: C.text2, marginTop: 4 }}>
                              Waiting for their response...
                            </div>
                          </div>
                        )}

                        {status === "incoming" && (
                          <div style={{ display: "flex", gap: 6 }}>
                            <button
                              type="button"
                              onClick={() => {
                                onAcceptRequest(searchResult.id);
                                showToast(`You are now connected with ${searchResult.name}`);
                              }}
                              style={{ background: "#22c55e", color: "#fff", border: "none", borderRadius: 8, padding: "7px 14px", fontSize: 12, fontWeight: 700, cursor: "pointer" }}
                            >
                              Accept
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                onRejectRequest(searchResult.id);
                                showToast("Connection request rejected");
                              }}
                              style={{ background: C.hover, color: C.text2, border: "none", borderRadius: 8, padding: "7px 12px", fontSize: 12, fontWeight: 600, cursor: "pointer" }}
                            >
                              Reject
                            </button>
                          </div>
                        )}

                        {(status === "none" || status === "rejected") && (
                          <button
                            type="button"
                            onClick={() => {
                              onSendRequest(searchResult.id);
                              showToast(`Connection request sent to ${searchResult.name}`);
                            }}
                            style={{
                              background: C.accent,
                              color: "#fff",
                              border: "none",
                              borderRadius: 10,
                              padding: "9px 18px",
                              fontSize: 13,
                              fontWeight: 700,
                              cursor: "pointer",
                              boxShadow: "0 3px 10px rgba(99,102,241,0.25)"
                            }}
                          >
                            + Send Request
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })()}
            </div>
          )}

          {/* Empty search guide */}
          {!searched && (
            <div style={{ textAlign: "center", padding: "16px 10px 6px", color: C.text2, fontSize: 13 }}>
              🔗 <span style={{ fontWeight: 600 }}>Find your friends</span> — Search for a friend using their email address above.
            </div>
          )}
        </div>

        {/* ─── Incoming Connection Requests ─── */}
        <div style={{ background: C.panel, borderRadius: 16, padding: "24px", border: `1px solid ${C.hover}`, marginBottom: 28, boxShadow: "0 4px 16px rgba(0,0,0,0.03)" }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 16 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span style={{ fontSize: 16, fontWeight: 800, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
                Connection Requests
              </span>
              {incomingRequests.length > 0 && (
                <span style={{ background: C.accent, color: "#fff", fontSize: 11, fontWeight: 800, padding: "2px 7px", borderRadius: 99 }}>
                  {incomingRequests.length}
                </span>
              )}
            </div>
            <span style={{ fontSize: 12, color: C.text2 }}>
              {incomingRequests.length} pending
            </span>
          </div>

          {incomingRequests.length === 0 ? (
            <div style={{ textAlign: "center", padding: "24px 10px", color: C.text2, fontSize: 13 }}>
              <div style={{ fontSize: 28, marginBottom: 6 }}>📬</div>
              No pending connection requests.
            </div>
          ) : (
            <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
              {incomingRequests.map(({ user }) => (
                <div
                  key={user.id}
                  style={{
                    background: C.card,
                    borderRadius: 14,
                    padding: "14px 16px",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    gap: 12,
                    flexWrap: "wrap",
                    border: `1px solid ${C.hover}`
                  }}
                >
                  <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
                    <Avatar user={user} size={44} showOnline />
                    <div>
                      <div style={{ fontWeight: 700, fontSize: 14, color: C.text1 }}>{user.name}</div>
                      <div style={{ fontSize: 12, color: C.text2 }}>{user.email}</div>
                      {user.bio && <div style={{ fontSize: 11, color: C.text2, marginTop: 2 }}>{user.bio}</div>}
                    </div>
                  </div>
                  <div style={{ display: "flex", gap: 8 }}>
                    <button
                      type="button"
                      onClick={() => {
                        onAcceptRequest(user.id);
                        showToast(`You are now connected with ${user.name}`);
                      }}
                      style={{
                        background: "#22c55e",
                        color: "#fff",
                        border: "none",
                        borderRadius: 10,
                        padding: "8px 16px",
                        fontSize: 13,
                        fontWeight: 700,
                        cursor: "pointer",
                        boxShadow: "0 2px 8px rgba(34, 197, 94, 0.25)"
                      }}
                    >
                      Accept
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        onRejectRequest(user.id);
                        showToast("Connection request rejected");
                      }}
                      style={{
                        background: C.panel,
                        color: C.text2,
                        border: `1px solid ${C.hover}`,
                        borderRadius: 10,
                        padding: "8px 14px",
                        fontSize: 13,
                        fontWeight: 600,
                        cursor: "pointer"
                      }}
                    >
                      Reject
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ─── My Connections ─── */}
        <div style={{ background: C.panel, borderRadius: 16, padding: "24px", border: `1px solid ${C.hover}`, marginBottom: 28, boxShadow: "0 4px 16px rgba(0,0,0,0.03)" }}>
          <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 16 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span style={{ fontSize: 16, fontWeight: 800, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
                My Connections
              </span>
              <span style={{ background: C.card, color: C.text2, fontSize: 11, fontWeight: 800, padding: "2px 8px", borderRadius: 99 }}>
                {connectedFriends.length}
              </span>
            </div>
            <span style={{ fontSize: 12, color: C.text2 }}>Connected friends you can message</span>
          </div>

          {connectedFriends.length === 0 ? (
            <div style={{ textAlign: "center", padding: "30px 10px", color: C.text2, fontSize: 13 }}>
              <div style={{ fontSize: 32, marginBottom: 8 }}>👥</div>
              No connections yet. Find and connect with your friends above.
            </div>
          ) : (
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: 12 }}>
              {connectedFriends.map(({ user }) => (
                <div
                  key={user.id}
                  style={{
                    background: C.card,
                    borderRadius: 14,
                    padding: "16px",
                    display: "flex",
                    flexDirection: "column",
                    justifyContent: "space-between",
                    gap: 12,
                    border: `1px solid ${C.hover}`,
                    transition: "transform 0.15s ease, box-shadow 0.15s ease"
                  }}
                >
                  <div style={{ display: "flex", gap: 12, alignItems: "flex-start" }}>
                    <Avatar user={user} size={44} showOnline />
                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                        <span style={{ fontWeight: 700, fontSize: 14, color: C.text1, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
                          {user.name}
                        </span>
                      </div>
                      <div style={{ fontSize: 12, color: C.accent, fontWeight: 600 }}>{user.username}</div>
                      <div style={{ fontSize: 11, color: C.text2, marginTop: 2 }}>{user.email}</div>
                    </div>
                  </div>

                  {user.bio && (
                    <div style={{ fontSize: 11, color: C.text2, lineHeight: 1.4, display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden" }}>
                      {user.bio}
                    </div>
                  )}

                  <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", paddingTop: 8, borderTop: `1px solid ${C.hover}` }}>
                    <div style={{ display: "flex", alignItems: "center", gap: 5, fontSize: 11, color: user.online ? C.online : C.offline, fontWeight: 600 }}>
                      <span style={{ width: 7, height: 7, borderRadius: "50%", background: user.online ? C.online : C.offline }} />
                      {user.online ? "Online" : "Offline"}
                    </div>
                    <button
                      type="button"
                      onClick={() => onMessageUser(user)}
                      style={{
                        background: C.accent,
                        color: "#fff",
                        border: "none",
                        borderRadius: 10,
                        padding: "7px 16px",
                        fontSize: 12,
                        fontWeight: 700,
                        cursor: "pointer",
                        display: "flex",
                        alignItems: "center",
                        gap: 6,
                        boxShadow: "0 2px 8px rgba(99,102,241,0.25)"
                      }}
                    >
                      <span>💬</span> Message
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── Change Password Page ─────────────────────────────────────────────────────
function ChangePasswordPage({ onBack }) {
  const { C, theme } = useTheme();
  const [currentPass, setCurrentPass] = useState("");
  const [newPass, setNewPass] = useState("");
  const [confirmPass, setConfirmPass] = useState("");
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [successMsg, setSuccessMsg] = useState("");

  const reqs = checkPasswordStrength(newPass);
  const passwordsMatch = newPass.length > 0 && newPass === confirmPass;
  const isConfirmDirty = confirmPass.length > 0;
  const isFormValid = currentPass.trim().length > 0 && reqs.isValid && passwordsMatch;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!isFormValid) return;
    setSuccessMsg("Password changed successfully.");
    setCurrentPass("");
    setNewPass("");
    setConfirmPass("");
  };

  const handleCancel = () => {
    setCurrentPass("");
    setNewPass("");
    setConfirmPass("");
    setSuccessMsg("");
    onBack();
  };

  return (
    <div style={{ flex: 1, overflowY: "auto", background: C.bg }}>
      {/* Header */}
      <div style={{ background: C.panel, height: 64, display: "flex", alignItems: "center", gap: 12, padding: "0 24px", borderBottom: `1px solid ${C.hover}` }}>
        <button
          onClick={handleCancel}
          style={{ background: "none", border: "none", color: C.text1, cursor: "pointer", fontSize: 20, display: "flex", alignItems: "center", padding: "6px" }}
          title="Back to Settings"
        >
          ←
        </button>
        <span style={{ fontWeight: 800, fontSize: 18, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Change Password</span>
      </div>

      <div style={{ maxWidth: 540, margin: "24px auto", padding: "0 24px" }}>
        {/* Success Banner */}
        {successMsg && (
          <div style={{
            background: "rgba(34, 197, 94, 0.12)",
            color: "#22c55e",
            borderRadius: 12,
            padding: "12px 16px",
            marginBottom: 20,
            border: "1.5px solid #22c55e",
            fontSize: 14,
            fontWeight: 700,
            display: "flex",
            alignItems: "center",
            gap: 8
          }}>
            <span style={{ fontSize: 16 }}>✓</span> {successMsg}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ background: C.panel, borderRadius: 16, padding: "24px", border: `1px solid ${C.hover}`, boxShadow: "0 4px 20px rgba(0,0,0,0.04)" }}>
          {/* Current Password */}
          <div style={{ marginBottom: 18 }}>
            <label style={{ display: "block", fontSize: 12, color: C.text2, fontWeight: 700, marginBottom: 6 }}>
              Current Password
            </label>
            <div style={{ position: "relative" }}>
              <input
                type={showCurrent ? "text" : "password"}
                value={currentPass}
                onChange={e => { setCurrentPass(e.target.value); setSuccessMsg(""); }}
                placeholder="Enter current password"
                style={{
                  width: "100%",
                  background: C.card,
                  border: `1px solid ${C.hover}`,
                  borderRadius: 12,
                  padding: "11px 44px 11px 14px",
                  color: C.text1,
                  fontSize: 14,
                  boxSizing: "border-box",
                  outline: "none"
                }}
              />
              <button
                type="button"
                onClick={() => setShowCurrent(!showCurrent)}
                style={{
                  position: "absolute",
                  right: 12,
                  top: "50%",
                  transform: "translateY(-50%)",
                  background: "none",
                  border: "none",
                  color: C.text2,
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  padding: 4
                }}
                title={showCurrent ? "Hide password" : "Show password"}
              >
                {showCurrent ? <Ic.eye /> : <Ic.eyeOff />}
              </button>
            </div>
          </div>

          {/* New Password */}
          <div style={{ marginBottom: 18 }}>
            <label style={{ display: "block", fontSize: 12, color: C.text2, fontWeight: 700, marginBottom: 6 }}>
              New Password
            </label>
            <div style={{ position: "relative" }}>
              <input
                type={showNew ? "text" : "password"}
                value={newPass}
                onChange={e => { setNewPass(e.target.value); setSuccessMsg(""); }}
                placeholder="Enter new password"
                style={{
                  width: "100%",
                  background: C.card,
                  border: `1px solid ${C.hover}`,
                  borderRadius: 12,
                  padding: "11px 44px 11px 14px",
                  color: C.text1,
                  fontSize: 14,
                  boxSizing: "border-box",
                  outline: "none"
                }}
              />
              <button
                type="button"
                onClick={() => setShowNew(!showNew)}
                style={{
                  position: "absolute",
                  right: 12,
                  top: "50%",
                  transform: "translateY(-50%)",
                  background: "none",
                  border: "none",
                  color: C.text2,
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  padding: 4
                }}
                title={showNew ? "Hide password" : "Show password"}
              >
                {showNew ? <Ic.eye /> : <Ic.eyeOff />}
              </button>
            </div>
          </div>

          {/* Confirm New Password */}
          <div style={{ marginBottom: 18 }}>
            <label style={{ display: "block", fontSize: 12, color: C.text2, fontWeight: 700, marginBottom: 6 }}>
              Confirm New Password
            </label>
            <div style={{ position: "relative" }}>
              <input
                type={showConfirm ? "text" : "password"}
                value={confirmPass}
                onChange={e => { setConfirmPass(e.target.value); setSuccessMsg(""); }}
                placeholder="Confirm new password"
                style={{
                  width: "100%",
                  background: C.card,
                  border: `1px solid ${isConfirmDirty && !passwordsMatch ? "#ef4444" : C.hover}`,
                  borderRadius: 12,
                  padding: "11px 44px 11px 14px",
                  color: C.text1,
                  fontSize: 14,
                  boxSizing: "border-box",
                  outline: "none"
                }}
              />
              <button
                type="button"
                onClick={() => setShowConfirm(!showConfirm)}
                style={{
                  position: "absolute",
                  right: 12,
                  top: "50%",
                  transform: "translateY(-50%)",
                  background: "none",
                  border: "none",
                  color: C.text2,
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  padding: 4
                }}
                title={showConfirm ? "Hide password" : "Show password"}
              >
                {showConfirm ? <Ic.eye /> : <Ic.eyeOff />}
              </button>
            </div>
            {/* Match error or match success */}
            {isConfirmDirty && (
              <div style={{ marginTop: 6, fontSize: 12, fontWeight: 600, color: passwordsMatch ? "#22c55e" : "#ef4444", display: "flex", alignItems: "center", gap: 5 }}>
                <span>{passwordsMatch ? "✓ Passwords match" : "❌ Passwords do not match"}</span>
              </div>
            )}
          </div>

          {/* Password Requirements Checklist */}
          <div style={{
            background: C.card,
            borderRadius: 12,
            padding: "14px",
            marginBottom: 24,
            border: `1px solid ${C.hover}`
          }}>
            <div style={{ fontSize: 12, fontWeight: 700, color: C.text1, marginBottom: 8 }}>
              Password Requirements:
            </div>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "6px 10px", fontSize: 12 }}>
              <div style={{ color: reqs.hasMinLen ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 6, fontWeight: reqs.hasMinLen ? 700 : 500 }}>
                <span>{reqs.hasMinLen ? "✓" : "○"}</span> At least 8 characters
              </div>
              <div style={{ color: reqs.hasUpper ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 6, fontWeight: reqs.hasUpper ? 700 : 500 }}>
                <span>{reqs.hasUpper ? "✓" : "○"}</span> At least one uppercase letter
              </div>
              <div style={{ color: reqs.hasLower ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 6, fontWeight: reqs.hasLower ? 700 : 500 }}>
                <span>{reqs.hasLower ? "✓" : "○"}</span> At least one lowercase letter
              </div>
              <div style={{ color: reqs.hasNumber ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 6, fontWeight: reqs.hasNumber ? 700 : 500 }}>
                <span>{reqs.hasNumber ? "✓" : "○"}</span> At least one number
              </div>
              <div style={{ color: reqs.hasSymbol ? "#22c55e" : C.text2, display: "flex", alignItems: "center", gap: 6, fontWeight: reqs.hasSymbol ? 700 : 500, gridColumn: "span 2" }}>
                <span>{reqs.hasSymbol ? "✓" : "○"}</span> At least one special character
              </div>
            </div>

            {newPass.length > 0 && (
              <div style={{ marginTop: 10, paddingTop: 8, borderTop: `1px solid ${C.hover}`, color: reqs.isValid ? "#22c55e" : "#ef4444", fontSize: 12, fontWeight: 700, display: "flex", alignItems: "center", gap: 6 }}>
                <span>{reqs.isValid ? "✓" : "❌"}</span>
                <span>{reqs.isValid ? "Password meets all requirements" : (!reqs.hasMinLen ? "Password must be at least 8 characters" : (!reqs.hasUpper ? "Password must contain at least one uppercase letter" : (!reqs.hasLower ? "Password must contain at least one lowercase letter" : (!reqs.hasNumber ? "Password must contain at least one number" : "Password must contain at least one special character"))))}</span>
              </div>
            )}
          </div>

          {/* Action Buttons */}
          <div style={{ display: "flex", gap: 12, justifyContent: "flex-end" }}>
            <button
              type="button"
              onClick={handleCancel}
              style={{
                background: C.card,
                border: `1px solid ${C.hover}`,
                borderRadius: 12,
                padding: "11px 22px",
                color: C.text2,
                fontSize: 14,
                fontWeight: 600,
                cursor: "pointer",
                transition: "all 0.15s"
              }}
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!isFormValid}
              style={{
                background: isFormValid ? C.accent : (theme === "dark" ? "#233152" : "#e2e8f0"),
                border: "none",
                borderRadius: 12,
                padding: "11px 24px",
                color: isFormValid ? "#fff" : (theme === "dark" ? "#64748b" : "#94a3b8"),
                fontSize: 14,
                fontWeight: 700,
                cursor: isFormValid ? "pointer" : "not-allowed",
                transition: "all 0.2s ease",
                boxShadow: isFormValid ? "0 4px 12px rgba(99,102,241,0.25)" : "none"
              }}
            >
              Change Password
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ─── Settings Page ────────────────────────────────────────────────────────────
function SettingsPage({ onLogout }) {
  const {
    theme, setTheme, C,
    lastSeenPrivacy, setLastSeenPrivacy,
    profilePhotoPrivacy, setProfilePhotoPrivacy
  } = useTheme();
  const [subPage, setSubPage] = useState(null); // null | "lastSeen" | "profilePhoto" | "changePassword"
  const [notifs, setNotifs] = useState(true);
  const [sounds, setSounds] = useState(true);
  const [preview, setPreview] = useState(true);

  const lastSeenOptions = [
    { id: "Everyone", label: "Everyone", desc: "Everyone can see your last seen and you can see everyone's last seen details." },
    { id: "My Contacts", label: "My Contacts", desc: "Show last seen details only for persons who are in your contacts." },
    { id: "Nobody", label: "Nobody", desc: "Nobody can see your last seen and all last seen details are hidden." },
  ];

  const profilePhotoOptions = [
    { id: "Everyone", label: "Everyone", desc: "Everyone can see your profile photo across all chats." },
    { id: "My Contacts", label: "My Contacts", desc: "Only persons in your contacts list can see your profile photo." },
    { id: "Nobody", label: "Nobody", desc: "Nobody can see your profile photo." },
  ];

  // Subpage: Change Password
  if (subPage === "changePassword") {
    return <ChangePasswordPage onBack={() => setSubPage(null)} />;
  }

  // Subpage: Last Seen
  if (subPage === "lastSeen") {
    return (
      <div style={{ flex: 1, overflowY: "auto", background: C.bg }}>
        <div style={{ background: C.panel, height: 64, display: "flex", alignItems: "center", gap: 12, padding: "0 24px", borderBottom: `1px solid ${C.hover}` }}>
          <button onClick={() => setSubPage(null)} style={{ background: "none", border: "none", color: C.text1, cursor: "pointer", fontSize: 20, display: "flex", alignItems: "center", padding: "6px" }}>
            ←
          </button>
          <span style={{ fontWeight: 800, fontSize: 18, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Last Seen</span>
        </div>
        <div style={{ maxWidth: 540, margin: "24px auto", padding: "0 24px" }}>
          <div style={{ fontSize: 12, color: C.text2, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: 12 }}>
            Who can see my last seen
          </div>
          <div style={{ background: C.panel, borderRadius: 14, overflow: "hidden", border: `1px solid ${C.hover}` }}>
            {lastSeenOptions.map((opt, i) => (
              <div
                key={opt.id}
                onClick={() => setLastSeenPrivacy(opt.id)}
                style={{
                  padding: "14px 18px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  cursor: "pointer",
                  borderBottom: i < lastSeenOptions.length - 1 ? `1px solid ${C.hover}` : "none",
                  background: lastSeenPrivacy === opt.id ? (theme === "dark" ? "rgba(128,131,255,0.15)" : "rgba(99,102,241,0.08)") : "transparent",
                  transition: "background 0.15s"
                }}
              >
                <div>
                  <div style={{ fontSize: 15, fontWeight: lastSeenPrivacy === opt.id ? 700 : 500, color: lastSeenPrivacy === opt.id ? C.accent : C.text1 }}>
                    {opt.label}
                  </div>
                  <div style={{ fontSize: 12, color: C.text2, marginTop: 2 }}>
                    {opt.desc}
                  </div>
                </div>
                {lastSeenPrivacy === opt.id && (
                  <span style={{ color: C.accent, fontWeight: 800, fontSize: 18, marginLeft: 12 }}>✓</span>
                )}
              </div>
            ))}
          </div>
          <div style={{ fontSize: 12, color: C.text2, marginTop: 12, lineHeight: 1.5 }}>
            {lastSeenPrivacy === "Everyone" && "Currently showing last seen for everyone."}
            {lastSeenPrivacy === "My Contacts" && "Currently showing last seen details only for persons in your contacts."}
            {lastSeenPrivacy === "Nobody" && "Currently last seen details are hidden for everyone."}
          </div>
        </div>
      </div>
    );
  }

  // Subpage: Profile Photo
  if (subPage === "profilePhoto") {
    return (
      <div style={{ flex: 1, overflowY: "auto", background: C.bg }}>
        <div style={{ background: C.panel, height: 64, display: "flex", alignItems: "center", gap: 12, padding: "0 24px", borderBottom: `1px solid ${C.hover}` }}>
          <button onClick={() => setSubPage(null)} style={{ background: "none", border: "none", color: C.text1, cursor: "pointer", fontSize: 20, display: "flex", alignItems: "center", padding: "6px" }}>
            ←
          </button>
          <span style={{ fontWeight: 800, fontSize: 18, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Profile Photo</span>
        </div>
        <div style={{ maxWidth: 540, margin: "24px auto", padding: "0 24px" }}>
          <div style={{ fontSize: 12, color: C.text2, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: 12 }}>
            Who can see my profile photo
          </div>
          <div style={{ background: C.panel, borderRadius: 14, overflow: "hidden", border: `1px solid ${C.hover}` }}>
            {profilePhotoOptions.map((opt, i) => (
              <div
                key={opt.id}
                onClick={() => setProfilePhotoPrivacy(opt.id)}
                style={{
                  padding: "14px 18px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                  cursor: "pointer",
                  borderBottom: i < profilePhotoOptions.length - 1 ? `1px solid ${C.hover}` : "none",
                  background: profilePhotoPrivacy === opt.id ? (theme === "dark" ? "rgba(128,131,255,0.15)" : "rgba(99,102,241,0.08)") : "transparent",
                  transition: "background 0.15s"
                }}
              >
                <div>
                  <div style={{ fontSize: 15, fontWeight: profilePhotoPrivacy === opt.id ? 700 : 500, color: profilePhotoPrivacy === opt.id ? C.accent : C.text1 }}>
                    {opt.label}
                  </div>
                  <div style={{ fontSize: 12, color: C.text2, marginTop: 2 }}>
                    {opt.desc}
                  </div>
                </div>
                {profilePhotoPrivacy === opt.id && (
                  <span style={{ color: C.accent, fontWeight: 800, fontSize: 18, marginLeft: 12 }}>✓</span>
                )}
              </div>
            ))}
          </div>
          <div style={{ fontSize: 12, color: C.text2, marginTop: 12, lineHeight: 1.5 }}>
            Choose who can view your profile picture across your chats and contacts.
          </div>
        </div>
      </div>
    );
  }

  const sections = [
    { title: "Appearance", items: [
      { label: "Theme", right: (
        <div style={{ display: "flex", gap: 6 }}>
          {["dark","light"].map(t => (
            <button
              key={t}
              onClick={() => setTheme(t)}
              style={{
                padding: "5px 14px",
                borderRadius: 8,
                border: "none",
                cursor: "pointer",
                fontSize: 12,
                fontWeight: 700,
                textTransform: "capitalize",
                background: theme === t ? C.accent : C.hover,
                color: theme === t ? "#fff" : C.text2,
                transition: "all 0.15s ease"
              }}
            >
              {t}
            </button>
          ))}
        </div>
      )}
    ]},
    { title: "Notifications", items: [
      { label: "Enable notifications", right: <Toggle val={notifs} set={setNotifs} /> },
      { label: "Sound effects", right: <Toggle val={sounds} set={setSounds} /> },
      { label: "Message preview", right: <Toggle val={preview} set={setPreview} /> },
    ]},
    { title: "Privacy", items: [
      { label: "Last seen", onClick: () => setSubPage("lastSeen"), right: <span style={{ fontSize: 13, color: C.text2, fontWeight: 600, display: "flex", alignItems: "center", gap: 4 }}>{lastSeenPrivacy} ›</span> },
      { label: "Profile photo", onClick: () => setSubPage("profilePhoto"), right: <span style={{ fontSize: 13, color: C.text2, fontWeight: 600, display: "flex", alignItems: "center", gap: 4 }}>{profilePhotoPrivacy} ›</span> },
    ]},
    { title: "Account", items: [
      { label: "Change password", onClick: () => setSubPage("changePassword"), right: <span style={{ fontSize: 13, color: C.text2, fontWeight: 600, display: "flex", alignItems: "center", gap: 4 }}>›</span> },
      { label: "Sign out", right: null, danger: true, onClick: onLogout },
    ]},
  ];

  return (
    <div style={{ flex: 1, overflowY: "auto", background: C.bg }}>
      <div style={{ background: C.panel, height: 64, display: "flex", alignItems: "center", padding: "0 24px", borderBottom: `1px solid ${C.hover}` }}>
        <span style={{ fontWeight: 800, fontSize: 18, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Settings</span>
      </div>
      <div style={{ maxWidth: 540, margin: "24px auto", padding: "0 24px" }}>
        {sections.map(sec => (
          <div key={sec.title} style={{ marginBottom: 24 }}>
            <div style={{ fontSize: 11, color: C.text2, fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: 8 }}>{sec.title}</div>
            {sec.items.map(item => (
              <div key={item.label} onClick={item.onClick} style={{ background: C.panel, borderRadius: 10, padding: "12px 14px", display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: 4, cursor: item.onClick || item.danger ? "pointer" : "default", transition: "background 0.15s" }}>
                <span style={{ fontSize: 14, color: item.danger ? C.danger : C.text1 }}>{item.label}</span>
                {item.right}
              </div>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

function Toggle({ val, set }) {
  const { C } = useTheme();
  return (
    <div onClick={() => set(!val)} style={{ width: 38, height: 22, background: val ? C.accent : C.hover, borderRadius: 99, position: "relative", cursor: "pointer", transition: "background 0.2s" }}>
      <div style={{ position: "absolute", top: 2, left: val ? 18 : 2, width: 18, height: 18, background: "#fff", borderRadius: "50%", transition: "left 0.2s" }} />
    </div>
  );
}

// ─── Groups Page ──────────────────────────────────────────────────────────────
function GroupsPage({ chats, selectedChat, onSelectChat, onUpdateChat }) {
  const { C } = useTheme();
  const [creating, setCreating] = useState(false);
  const [newName, setNewName] = useState("");
  const [selectedMembers, setSelectedMembers] = useState([]);

  const groups = chats.filter(c => c.type === "group");

  const createGroup = (addChat) => {
    if (!newName.trim() || selectedMembers.length === 0) return;
    const g = {
      id: `g${Date.now()}`, type: "group", name: newName,
      initials: newName.slice(0,2).toUpperCase(), color: C.groupBg,
      members: selectedMembers,
      messages: [{ id: "m0", from: "me", type: "text", text: `Group "${newName}" created!`, time: new Date().toLocaleTimeString([], {hour:"2-digit",minute:"2-digit"}), status: "sent" }],
      lastMessage: `Group "${newName}" created!`, lastTime: "Just now", unread: 0
    };
    addChat(g);
    setCreating(false); setNewName(""); setSelectedMembers([]);
  };

  return (
    <div style={{ flex: 1, overflowY: "auto" }}>
      <div style={{ background: C.panel, height: 64, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 24px", borderBottom: `1px solid ${C.hover}` }}>
        <span style={{ fontWeight: 800, fontSize: 18, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Groups</span>
        <button onClick={() => setCreating(true)}
          style={{ background: C.accent, border: "none", borderRadius: 10, padding: "8px 14px", fontSize: 13, color: "#fff", fontWeight: 700, cursor: "pointer", display: "flex", gap: 6, alignItems: "center" }}>
          <Ic.plus />New Group
        </button>
      </div>
      <div style={{ padding: 24 }}>
        {groups.length === 0 && <div style={{ color: C.text2, fontSize: 14, textAlign: "center", marginTop: 48 }}>No groups yet. Create one!</div>}
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: 12 }}>
          {groups.map(g => (
            <div key={g.id} onClick={() => onSelectChat(g)}
              style={{ background: C.panel, borderRadius: 16, padding: 16, cursor: "pointer", border: selectedChat?.id === g.id ? `2px solid ${C.accent}` : `2px solid transparent`, transition: "border 0.15s" }}>
              <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 10 }}>
                <div style={{ width: 48, height: 48, borderRadius: "50%", background: g.color, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18, fontWeight: 800, color: C.text3, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>{g.initials}</div>
                <div>
                  <div style={{ fontWeight: 700, fontSize: 15, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>{g.name}</div>
                  <div style={{ fontSize: 12, color: C.text2 }}>{g.members.length} members</div>
                </div>
              </div>
              <div style={{ fontSize: 12, color: C.text2, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{g.lastMessage}</div>
              <div style={{ display: "flex", gap: -8, marginTop: 10 }}>
                {g.members.slice(0,4).map((uid, i) => {
                  const u = getUser(uid);
                  return <div key={uid} style={{ marginLeft: i > 0 ? -6 : 0, borderRadius: "50%", border: `2px solid ${C.panel}`, zIndex: 4 - i }}>
                    <Avatar user={u} size={24} />
                  </div>;
                })}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Create Group Modal */}
      {creating && (
        <div style={{ position: "fixed", inset: 0, background: C.modalOverlay, display: "flex", alignItems: "center", justifyContent: "center", zIndex: 100 }}>
          <div style={{ width: 400, background: C.sidebar, borderRadius: 20, padding: 24, boxShadow: "0 24px 48px rgba(0,0,0,0.12)", border: `1px solid ${C.hover}` }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
              <span style={{ fontWeight: 700, fontSize: 16, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Create Group</span>
              <button onClick={() => setCreating(false)} style={{ background: "none", border: "none", color: C.text2, cursor: "pointer" }}><Ic.close /></button>
            </div>
            <label style={{ display: "block", fontSize: 12, color: C.text2, fontWeight: 600, marginBottom: 6 }}>Group Name</label>
            <input value={newName} onChange={e => setNewName(e.target.value)} placeholder="e.g. Design Team"
              style={{ width: "100%", background: C.card, border: `1px solid ${C.hover}`, borderRadius: 12, padding: "10px 14px", color: C.text1, fontSize: 14, boxSizing: "border-box", outline: "none", marginBottom: 16 }} />
            <label style={{ display: "block", fontSize: 12, color: C.text2, fontWeight: 600, marginBottom: 8 }}>Add Members</label>
            <div style={{ display: "flex", flexDirection: "column", gap: 4, marginBottom: 16, maxHeight: 180, overflowY: "auto" }}>
              {USERS.map(u => (
                <label key={u.id} style={{ display: "flex", gap: 10, alignItems: "center", padding: "8px 10px", background: C.card, borderRadius: 10, cursor: "pointer", border: `1px solid ${C.hover}` }}>
                  <input type="checkbox" checked={selectedMembers.includes(u.id)} onChange={e => {
                    setSelectedMembers(prev => e.target.checked ? [...prev, u.id] : prev.filter(id => id !== u.id));
                  }} style={{ accentColor: C.accent }} />
                  <Avatar user={u} size={30} />
                  <span style={{ fontSize: 13, color: C.text1, fontWeight: 600 }}>{u.name}</span>
                </label>
              ))}
            </div>
            <button onClick={() => createGroup((g) => {})}
              style={{ width: "100%", background: C.accent, border: "none", borderRadius: 12, padding: "12px", color: "#fff", fontSize: 14, fontWeight: 700, cursor: "pointer", boxShadow: "0 4px 12px rgba(99,102,241,0.25)" }}>
              Create Group
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

// ─── Main App Content ─────────────────────────────────────────────────────────
function AppContent() {
  const { C, userProfile } = useTheme();
  const [page, setPage] = useState("login"); // login | register | app
  const [activeNav, setActiveNav] = useState("Chats");
  const [chats, setChats] = useState(INITIAL_CHATS);
  const [selectedChat, setSelectedChat] = useState(INITIAL_CHATS[0]);
  const [filter, setFilter] = useState("All");
  const [windowWidth, setWindowWidth] = useState(window.innerWidth);
  const statuses = INITIAL_STATUSES;

  useEffect(() => {
    const handler = () => setWindowWidth(window.innerWidth);
    window.addEventListener("resize", handler);
    return () => window.removeEventListener("resize", handler);
  }, []);

  const isMobile = windowWidth < 768;
  const isTablet = windowWidth >= 768 && windowWidth < 1100;

  const updateChat = (chatId, newMessages) => {
    setChats(prev => prev.map(c => c.id === chatId ? {
      ...c, messages: newMessages,
      lastMessage: newMessages[newMessages.length - 1]?.text || (newMessages[newMessages.length - 1]?.type || ""),
      lastTime: newMessages[newMessages.length - 1]?.time || c.lastTime
    } : c));
    if (selectedChat?.id === chatId) {
      setSelectedChat(prev => ({
        ...prev, messages: newMessages,
        lastMessage: newMessages[newMessages.length - 1]?.text || prev.lastMessage,
        lastTime: newMessages[newMessages.length - 1]?.time || prev.lastTime
      }));
    }
  };

  const addChat = (newChat) => {
    setChats(prev => [newChat, ...prev]);
  };

  const deleteChat = (chatId) => {
    setChats(prev => prev.filter(c => c.id !== chatId));
    if (selectedChat?.id === chatId) {
      setSelectedChat(null);
    }
  };

  const selectChat = (chat) => {
    setSelectedChat(chat);
    setChats(prev => prev.map(c => c.id === chat.id ? { ...c, unread: 0 } : c));
    if (activeNav !== "Chats") setActiveNav("Chats");
  };

  const INITIAL_CONNECTIONS = [
    { userId: "1", status: "connected" }, // Arun Kumar (Connected)
    { userId: "2", status: "connected" }, // Priya Sharma (Connected)
    { userId: "4", status: "incoming" },  // Rahul Nair (Incoming Request)
    { userId: "6", status: "incoming" },  // Sneha Patel (Incoming Request)
    { userId: "3", status: "sent" },      // Maya Chen (Request Sent / Pending)
  ];

  const [connections, setConnections] = useState(() => {
    try {
      const saved = localStorage.getItem("connectly_connections");
      if (saved) return JSON.parse(saved);
    } catch {}
    return INITIAL_CONNECTIONS;
  });

  const saveConnections = (newConns) => {
    setConnections(newConns);
    try {
      localStorage.setItem("connectly_connections", JSON.stringify(newConns));
    } catch {}
  };

  const handleSendRequest = (userId) => {
    const updated = connections.filter(c => c.userId !== userId).concat({ userId, status: "sent" });
    saveConnections(updated);
  };

  const handleAcceptRequest = (userId) => {
    const updated = connections.filter(c => c.userId !== userId).concat({ userId, status: "connected" });
    saveConnections(updated);
  };

  const handleRejectRequest = (userId) => {
    const updated = connections.filter(c => c.userId !== userId).concat({ userId, status: "rejected" });
    saveConnections(updated);
  };

  const handleMessageUser = (user) => {
    const existing = chats.find(c => c.type === "direct" && c.userId === user.id);
    if (existing) {
      selectChat(existing);
    } else {
      const newChat = {
        id: `c_${Date.now()}`,
        type: "direct",
        userId: user.id,
        messages: [],
        lastMessage: "Start a conversation",
        lastTime: "Just now",
        unread: 0
      };
      addChat(newChat);
      selectChat(newChat);
    }
    setActiveNav("Chats");
  };

  const incomingCount = connections.filter(c => c.status === "incoming").length;

  const navItems = [
    { label: "Chats", icon: Ic.chat },
    { label: "Groups", icon: Ic.group },
    { label: "Status", icon: Ic.status },
    { label: "Profile", icon: Ic.profile },
    { label: "Connections", icon: Ic.connections, badge: incomingCount },
    { label: "Settings", icon: Ic.settings },
  ];

  if (page === "login") return <LoginPage onLogin={() => setPage("app")} onGoRegister={() => setPage("register")} />;
  if (page === "register") return <RegisterPage onRegister={() => setPage("app")} onGoLogin={() => setPage("login")} />;

  const currentChat = chats.find(c => c.id === selectedChat?.id) || selectedChat;

  // Mobile: show list OR chat
  const showMobileList = isMobile && (!selectedChat || activeNav !== "Chats");

  const userInitials = (userProfile?.name || "You").trim().split(" ").map(w => w[0]).join("").slice(0, 2).toUpperCase() || "YO";

  return (
    <div style={{ height: "100vh", display: "flex", flexDirection: "column", background: C.bg, fontFamily: "'Inter', sans-serif", overflow: "hidden" }}>
      {/* Top Nav Bar */}
      <div style={{ background: C.topNavBg, backdropFilter: "blur(12px)", height: 60, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 24px", borderBottom: `1px solid ${C.hover}`, flexShrink: 0, zIndex: 50 }}>
        <div style={{ display: "flex", gap: 10, alignItems: "center", minWidth: 160 }}>
          <span style={{ fontSize: 22, fontWeight: 800, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif", letterSpacing: "-0.5px" }}>Connectly</span>
        </div>
        {!isMobile && (
          <nav style={{ display: "flex", gap: 6, justifyContent: "center", flex: 1 }}>
            {navItems.map(({ label, badge }) => (
              <button key={label} onClick={() => setActiveNav(label)}
                style={{ background: activeNav === label ? C.card : "transparent", border: "none", borderRadius: 10, padding: "7px 16px", fontSize: 14, fontWeight: activeNav === label ? 700 : 500, color: activeNav === label ? C.text1 : C.text2, cursor: "pointer", transition: "all 0.15s", display: "flex", alignItems: "center", gap: 6 }}>
                <span>{label}</span>
                {badge > 0 && (
                  <span style={{ background: C.accent, color: "#fff", fontSize: 10, fontWeight: 800, padding: "1px 6px", borderRadius: 99 }}>
                    {badge}
                  </span>
                )}
              </button>
            ))}
          </nav>
        )}
        <div style={{ minWidth: 160 }} />
      </div>

      {/* Main body */}
      <div style={{ display: "flex", flex: 1, overflow: "hidden" }}>
        {/* Left icon nav (desktop/tablet) */}
        {!isMobile && (
          <div style={{ width: 70, background: C.sidebar, display: "flex", flexDirection: "column", alignItems: "center", padding: "16px 0", gap: 8, borderRight: `1px solid ${C.hover}`, flexShrink: 0 }}>
            {navItems.map(({ label, icon: Icon, badge }) => (
              <button key={label} onClick={() => setActiveNav(label)}
                title={label}
                style={{ width: 46, height: 46, borderRadius: 12, border: "none", display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer", transition: "all 0.15s", position: "relative",
                  background: activeNav === label ? C.accent : "transparent", color: activeNav === label ? "#fff" : C.text2 }}>
                <Icon />
                {badge > 0 && (
                  <span style={{ position: "absolute", top: 6, right: 6, width: 8, height: 8, borderRadius: "50%", background: activeNav === label ? "#fff" : C.accent, border: `2px solid ${activeNav === label ? C.accent : C.sidebar}` }} />
                )}
              </button>
            ))}
            {/* Bottom profile */}
            <div style={{ marginTop: "auto", paddingBottom: 8 }}>
              <div onClick={() => setActiveNav("Profile")}
                title={userProfile?.name || "Profile"}
                style={{ width: 38, height: 38, borderRadius: "50%", background: C.groupBg, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 12, fontWeight: 800, color: "#fff", cursor: "pointer", border: `2px solid ${C.hover}`, overflow: "hidden" }}>
                {userProfile?.avatar ? (
                  <img src={userProfile.avatar} alt="Me" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                ) : (
                  userInitials
                )}
              </div>
            </div>
          </div>
        )}

        {/* Content area */}
        <div style={{ flex: 1, display: "flex", overflow: "hidden" }}>
          {/* Chats section */}
          {activeNav === "Chats" && (
            <>
              {(!isMobile || showMobileList) && (
                <ChatList
                  chats={chats} selected={currentChat}
                  onSelect={selectChat}
                  filter={filter} onFilterChange={setFilter}
                />
              )}
              {currentChat && (!isMobile || !showMobileList) && (
                <ChatView
                  chat={currentChat} chats={chats}
                  onUpdateChat={updateChat}
                  onDeleteChat={deleteChat}
                  onBack={() => setSelectedChat(null)}
                  isMobile={isMobile}
                />
              )}
              {!currentChat && !isMobile && (
                <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", background: C.bg, color: C.text2, gap: 12 }}>
                  <div style={{ fontSize: 48 }}>💬</div>
                  <div style={{ fontSize: 18, fontWeight: 700, color: C.text1, fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Select a conversation</div>
                  <div style={{ fontSize: 14 }}>Choose from your chats on the left</div>
                </div>
              )}
            </>
          )}

          {activeNav === "Groups" && (
            <div style={{ flex: 1, display: "flex", overflow: "hidden" }}>
              {!isMobile && (
                <ChatList chats={chats.filter(c => c.type === "group")} selected={currentChat}
                  onSelect={selectChat} filter="Groups" onFilterChange={() => {}} />
              )}
              {currentChat?.type === "group" && !isMobile ? (
                <ChatView chat={currentChat} chats={chats} onUpdateChat={updateChat} onDeleteChat={deleteChat} onBack={() => setSelectedChat(null)} isMobile={isMobile} />
              ) : (
                <GroupsPage chats={chats} selectedChat={selectedChat} onSelectChat={selectChat} onUpdateChat={updateChat} />
              )}
            </div>
          )}

          {activeNav === "Status" && <StatusPage statuses={statuses} onAddStatus={() => {}} />}
          {activeNav === "Profile" && <ProfilePage />}
          {activeNav === "Connections" && (
            <ConnectionsPage
              connections={connections}
              onSendRequest={handleSendRequest}
              onAcceptRequest={handleAcceptRequest}
              onRejectRequest={handleRejectRequest}
              onMessageUser={handleMessageUser}
            />
          )}
          {activeNav === "Settings" && (
            <SettingsPage onLogout={() => {
              setPage("login");
              setActiveNav("Chats");
              setSelectedChat(null);
            }} />
          )}
        </div>
      </div>

      {/* Mobile bottom nav */}
      {isMobile && (
        <div style={{ background: C.sidebar, borderTop: `1px solid ${C.hover}`, display: "flex", justifyContent: "space-around", padding: "8px 0 12px" }}>
          {navItems.map(({ label, icon: Icon, badge }) => (
            <button key={label} onClick={() => { setActiveNav(label); if (label === "Chats") setSelectedChat(null); }}
              style={{ background: "none", border: "none", display: "flex", flexDirection: "column", alignItems: "center", gap: 3, cursor: "pointer", color: activeNav === label ? C.accent : C.text2, padding: "4px 8px", position: "relative" }}>
              <div style={{ position: "relative" }}>
                <Icon />
                {badge > 0 && (
                  <span style={{ position: "absolute", top: -2, right: -6, background: C.accent, color: "#fff", fontSize: 9, fontWeight: 800, padding: "0 4px", borderRadius: 99 }}>
                    {badge}
                  </span>
                )}
              </div>
              <span style={{ fontSize: 10, fontWeight: activeNav === label ? 700 : 400 }}>{label}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

// ─── Main App with Theme & Privacy Provider ───────────────────────────────────
function App() {
  const [theme, setTheme] = useState(() => {
    try {
      return localStorage.getItem("connectly_theme") || "light";
    } catch {
      return "light";
    }
  });

  const [lastSeenPrivacy, setLastSeenPrivacy] = useState(() => {
    try {
      return localStorage.getItem("connectly_privacy_last_seen") || "Everyone";
    } catch {
      return "Everyone";
    }
  });

  const [profilePhotoPrivacy, setProfilePhotoPrivacy] = useState(() => {
    try {
      return localStorage.getItem("connectly_privacy_profile_photo") || "Everyone";
    } catch {
      return "Everyone";
    }
  });

  const [userProfile, setUserProfile] = useState(() => {
    try {
      const saved = localStorage.getItem("connectly_user_profile");
      if (saved) {
        return JSON.parse(saved);
      }
    } catch {}
    return {
      name: "You",
      username: "@you",
      bio: "Building things on the internet 🛠️",
      avatar: null,
    };
  });

  const handleSetTheme = (t) => {
    setTheme(t);
    try {
      localStorage.setItem("connectly_theme", t);
    } catch {}
  };

  const handleSetLastSeenPrivacy = (val) => {
    setLastSeenPrivacy(val);
    try {
      localStorage.setItem("connectly_privacy_last_seen", val);
    } catch {}
  };

  const handleSetProfilePhotoPrivacy = (val) => {
    setProfilePhotoPrivacy(val);
    try {
      localStorage.setItem("connectly_privacy_profile_photo", val);
    } catch {}
  };

  const handleSetUserProfile = (valOrUpdater) => {
    setUserProfile((prev) => {
      const updated = typeof valOrUpdater === "function" ? valOrUpdater(prev) : { ...prev, ...valOrUpdater };
      try {
        localStorage.setItem("connectly_user_profile", JSON.stringify(updated));
      } catch {}
      return updated;
    });
  };

  const C = THEMES[theme] || THEMES.light;

  return (
    <ThemeContext.Provider value={{
      theme,
      setTheme: handleSetTheme,
      C,
      lastSeenPrivacy,
      setLastSeenPrivacy: handleSetLastSeenPrivacy,
      profilePhotoPrivacy,
      setProfilePhotoPrivacy: handleSetProfilePhotoPrivacy,
      userProfile,
      setUserProfile: handleSetUserProfile,
    }}>
      <AppContent />
    </ThemeContext.Provider>
  );
}


ReactDOM.createRoot(document.getElementById('root')).render(<App />);
