import {
  Suspense,
  lazy,
  useEffect,
  useMemo,
  useState,
  type FormEvent,
} from "react";
import {
  BrowserRouter,
  Link,
  Route,
  Routes,
  useNavigate,
  useParams,
} from "react-router-dom";
import {
  ArrowDown,
  BarChart3,
  ArrowLeft,
  ArrowRight,
  ArrowUpRight,
  Check,
  ClipboardList,
  Clock3,
  Coffee,
  Leaf,
  MapPin,
  Menu as MenuIcon,
  Minus,
  Plus,
  Search,
  ShoppingBag,
  UserRound,
  Wheat,
  X,
} from "lucide-react";
import { menuItems, type MenuItem } from "./data/menu";
import { useCart } from "./store/cart";
import { api } from "./api/client";
import { useAuth } from "./store/auth";
import { io } from "socket.io-client";
import "./App.css";
const BagelScene = lazy(() =>
  import("./components/BagelScene").then(({ BagelScene: Scene }) => ({
    default: Scene,
  })),
);

const categories = ["Everything", "Bagels", "Plates", "Coffee"] as const;
type Category = (typeof categories)[number];
const craftSteps = [
  {
    number: "01",
    title: "The mix",
    copy: "Stone-milled flour, cool water, a little salt. Mixed by hand, never hurried.",
    Icon: Wheat,
    image: "photo-1608198093002-ad4e005484a0",
  },
  {
    number: "02",
    title: "The wait",
    copy: "Our dough rests for 24 slow hours. Time makes the flavor; we just let it.",
    Icon: Clock3,
    image: "photo-1555507036-ab1f4038808a",
  },
  {
    number: "03",
    title: "The roll",
    copy: "Hand rolled, kettle boiled, and tucked into the oven while the city wakes.",
    Icon: Coffee,
    image: "photo-1509440159596-0249088772ff",
  },
  {
    number: "04",
    title: "The share",
    copy: "Still a little warm when it finds you. That part matters most.",
    Icon: Leaf,
    image: "photo-1565299624946-b28f40a0ae38",
  },
];
const adminToday = new Intl.DateTimeFormat("en-US", {
  weekday: "long",
  month: "long",
  day: "numeric",
}).format(new Date());
const currencyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
});

function formatCurrency(amount: number) {
  return currencyFormatter.format(amount);
}

function Storefront() {
  const staticHero =
    window.matchMedia("(prefers-reduced-motion: reduce)").matches ||
    (navigator.hardwareConcurrency > 0 && navigator.hardwareConcurrency <= 4);
  const [catalog, setCatalog] = useState(menuItems);
  const [accountOpen, setAccountOpen] = useState(false);
  const [accountMode, setAccountMode] = useState<"login" | "register">("login");
  const [authError, setAuthError] = useState("");
  const [demoOrder, setDemoOrder] = useState(true);
  const accessToken = useAuth((state) => state.accessToken);
  const accountEmail = useAuth((state) => state.email);
  const setSession = useAuth((state) => state.setSession);
  const clearSession = useAuth((state) => state.clearSession);
  const [activeCategory, setActiveCategory] = useState<Category>("Everything");
  const [search, setSearch] = useState("");
  const [vegetarianOnly, setVegetarianOnly] = useState(false);
  const [maxPrice, setMaxPrice] = useState(500);
  const [cartOpen, setCartOpen] = useState(false);
  const [checkoutOpen, setCheckoutOpen] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [orderId, setOrderId] = useState("");
  const [orderPlaced, setOrderPlaced] = useState(false);
  const [couponCode, setCouponCode] = useState("");
  const [appliedCoupon, setAppliedCoupon] = useState<{
    code: string;
    discountPercent: number;
  } | null>(null);
  const [couponMessage, setCouponMessage] = useState("");
  const items = useCart((state) => state.items);
  const add = useCart((state) => state.add);
  const update = useCart((state) => state.update);
  const clear = useCart((state) => state.clear);
  const itemCount = items.reduce((sum, entry) => sum + entry.quantity, 0);
  const subtotal = items.reduce(
    (sum, entry) => sum + entry.item.price * entry.quantity,
    0,
  );
  const filteredItems = useMemo(
    () =>
      catalog.filter((item) => {
        const matchesCategory =
          activeCategory === "Everything" || item.category === activeCategory;
        const matchesSearch = `${item.name} ${item.description}`
          .toLowerCase()
          .includes(search.toLowerCase());
        return (
          matchesCategory &&
          matchesSearch &&
          (!vegetarianOnly || item.vegetarian) &&
          item.price <= maxPrice
        );
      }),
    [activeCategory, search, vegetarianOnly, catalog, maxPrice],
  );
  useEffect(() => {
    api
      .get("/menu", { params: { page: 0, size: 50 } })
      .then(({ data }) => {
        if (Array.isArray(data.content) && data.content.length) {
          setCatalog(
            data.content.map(
              (item: Omit<MenuItem, "image"> & { imageUrl?: string }) => ({
                ...item,
                image: item.imageUrl ?? "",
              }),
            ),
          );
        }
      })
      .catch(() => undefined);
  }, []);

  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    let cancelled = false;
    let cleanup = () => {};
    void Promise.all([
      import("lenis"),
      import("gsap"),
      import("gsap/ScrollTrigger"),
    ])
      .then(([lenisModule, gsapModule, scrollModule]) => {
        if (cancelled) return;
        const lenis = new lenisModule.default({ smoothWheel: true });
        const { gsap } = gsapModule;
        const { ScrollTrigger } = scrollModule;
        gsap.registerPlugin(ScrollTrigger);
        lenis.on("scroll", ScrollTrigger.update);
        const updateScroll = (time: number) => lenis.raf(time * 1000);
        gsap.ticker.add(updateScroll);
        gsap.ticker.lagSmoothing(0);
        const context = gsap.context(() => {
          gsap.utils.toArray<HTMLElement>(".reveal").forEach((element) =>
            gsap.fromTo(
              element,
              { y: 26, opacity: 0 },
              {
                y: 0,
                opacity: 1,
                duration: 0.75,
                ease: "power2.out",
                scrollTrigger: {
                  trigger: element,
                  start: "top 88%",
                  once: true,
                },
              },
            ),
          );
        });
        cleanup = () => {
          context.revert();
          gsap.ticker.remove(updateScroll);
          lenis.destroy();
        };
      })
      .catch(() => undefined);
    return () => {
      cancelled = true;
      cleanup();
    };
  }, []);

  function addItem(item: MenuItem) {
    add(item);
    setCartOpen(true);
  }
  async function applyCoupon() {
    const code = couponCode.trim().toUpperCase();
    if (!code) return;
    try {
      const { data } = await api.get(`/coupons/${encodeURIComponent(code)}`);
      setAppliedCoupon({
        code: data.code,
        discountPercent: Number(data.discountPercent),
      });
      setCouponMessage(`${Number(data.discountPercent)}% off, lovely.`);
    } catch {
      if (code === "HELLOAUBE") {
        setAppliedCoupon({ code, discountPercent: 15 });
        setCouponMessage("15% off, lovely. Preview offer applied.");
      } else {
        setAppliedCoupon(null);
        setCouponMessage("That code could not be found.");
      }
    }
  }
  async function submitAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const values = new FormData(event.currentTarget);
    const email = String(values.get("email"));
    const endpoint =
      accountMode === "register" ? "/auth/register" : "/auth/login";
    const body =
      accountMode === "register"
        ? {
            name: String(values.get("name")),
            email,
            password: String(values.get("password")),
          }
        : { email, password: String(values.get("password")) };
    try {
      const { data } = await api.post(endpoint, body);
      setSession(data.accessToken, data.refreshToken, email);
      setAuthError("");
      setAccountOpen(false);
    } catch {
      setAuthError(
        "The account service could not sign you in. Check the details and try again.",
      );
    }
  }

  async function placeOrder(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setAuthError("");
    let id = `AU-${Math.random().toString(36).slice(2, 8).toUpperCase()}`;
    if (accessToken) {
      const values = new FormData(event.currentTarget);
      try {
        const { data } = await api.post("/orders", {
          items: items.map(({ item, quantity }) => ({
            menuItemId: item.id,
            quantity,
          })),
          deliveryAddress: String(values.get("address")),
          paymentMethod: "COD",
          couponCode: appliedCoupon?.code,
        });
        id = data.id;
        setDemoOrder(false);
      } catch {
        setAuthError(
          "We could not place that order. Please check your connection and try again.",
        );
        return;
      }
    } else {
      setDemoOrder(true);
    }
    setOrderId(id);
    setOrderPlaced(true);
    setCheckoutOpen(false);
    setCartOpen(false);
    clear();
  }

  return (
    <div className="site-shell" id="top">
      <div className="announcement">
        <span>Freshly baked, every single morning.</span>
        <a href="#menu">
          Order before 11 for same-day delivery <ArrowRight size={13} />
        </a>
      </div>
      <header className="site-header">
        <Link to="/" className="wordmark" aria-label="Aube Eats home">
          aube<span>®</span>
        </Link>
        <nav
          className={mobileMenuOpen ? "main-nav is-open" : "main-nav"}
          aria-label="Main navigation"
        >
          <a href="#story" onClick={() => setMobileMenuOpen(false)}>
            Our story
          </a>
          <a href="#craft" onClick={() => setMobileMenuOpen(false)}>
            The good stuff
          </a>
          <a href="#menu" onClick={() => setMobileMenuOpen(false)}>
            Menu
          </a>
          <a href="#visit" onClick={() => setMobileMenuOpen(false)}>
            Find us
          </a>
        </nav>
        <div className="header-actions">
          <button
            className="account-button"
            onClick={() => {
              setAuthError("");
              setAccountOpen(true);
            }}
            aria-label={accountEmail ? `Account ${accountEmail}` : "Sign in"}
          >
            <UserRound size={16} />
            <span>{accountEmail ?? "Sign in"}</span>
          </button>
          {accountOpen && (
            <div
              className="overlay"
              onMouseDown={(event) => {
                if (event.target === event.currentTarget) setAccountOpen(false);
              }}
            >
              <div className="checkout-modal account-modal">
                <div className="drawer-heading">
                  <div>
                    <span className="eyebrow">Aube account</span>
                    <h2>
                      {accessToken
                        ? "Welcome back."
                        : accountMode === "register"
                          ? "Come on in."
                          : "Good to see you."}
                    </h2>
                  </div>
                  <button
                    className="icon-button"
                    onClick={() => setAccountOpen(false)}
                    aria-label="Close account"
                  >
                    <X size={19} />
                  </button>
                </div>
                {accessToken ? (
                  <div className="account-session">
                    <p>
                      Signed in as <strong>{accountEmail}</strong>
                    </p>
                    <button
                      className="button button-dark checkout-button"
                      onClick={() => {
                        clearSession();
                        setAccountOpen(false);
                      }}
                    >
                      Sign out <ArrowRight size={16} />
                    </button>
                  </div>
                ) : (
                  <form onSubmit={submitAuth} className="checkout-form">
                    {accountMode === "register" && (
                      <label>
                        Your name
                        <input
                          required
                          name="name"
                          autoComplete="name"
                          maxLength={120}
                          placeholder="First and last name"
                        />
                      </label>
                    )}
                    <label>
                      Email address
                      <input
                        required
                        name="email"
                        type="email"
                        autoComplete="email"
                        placeholder="you@example.com"
                      />
                    </label>
                    <label>
                      Password
                      <input
                        required
                        name="password"
                        type="password"
                        autoComplete={
                          accountMode === "register"
                            ? "new-password"
                            : "current-password"
                        }
                        minLength={10}
                        maxLength={72}
                        placeholder="At least 10 characters"
                      />
                    </label>
                    {authError && (
                      <p className="auth-error" role="alert">
                        {authError}
                      </p>
                    )}
                    <button
                      className="button button-dark checkout-button"
                      type="submit"
                    >
                      {accountMode === "register"
                        ? "Create my account"
                        : "Sign in"}{" "}
                      <ArrowRight size={16} />
                    </button>
                    <button
                      type="button"
                      className="keep-shopping"
                      onClick={() => {
                        setAccountMode(
                          accountMode === "login" ? "register" : "login",
                        );
                        setAuthError("");
                      }}
                    >
                      {accountMode === "register"
                        ? "Already have an account? Sign in"
                        : "New around here? Create an account"}
                    </button>
                  </form>
                )}
              </div>
            </div>
          )}
          <button
            className="bag-button"
            onClick={() => setCartOpen(true)}
            aria-label={`Open bag, ${itemCount} items`}
          >
            <ShoppingBag size={18} />
            <span>Bag</span>
            <b>{itemCount}</b>
          </button>
          <button
            className="mobile-menu-button"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label={mobileMenuOpen ? "Close navigation" : "Open navigation"}
          >
            {mobileMenuOpen ? <X size={21} /> : <MenuIcon size={21} />}
          </button>
        </div>
      </header>

      <main>
        <section className="hero" aria-labelledby="hero-title">
          <div className="hero-copy">
            <span className="eyebrow">
              <span className="eyebrow-dot" /> A little more morning
            </span>
            <h1 id="hero-title">
              Good things
              <br />
              take <em>their</em> time.
            </h1>
            <p>
              Slow-made bagels, thoughtful toppings, and the kind of coffee that
              makes the first sip feel like a deep breath.
            </p>
            <div className="hero-actions">
              <a className="button button-dark" href="#menu">
                Find your favorite <ArrowRight size={16} />
              </a>
              <span className="hero-note">
                Baked fresh daily
                <br />
                in Brooklyn, NY
              </span>
            </div>
            <div className="hero-rating">
              <span className="rating-stars">★★★★★</span>
              <span>
                4.9 / 5 <i>from our morning people</i>
              </span>
            </div>
          </div>
          <div
            className="hero-art"
            aria-label="A golden sesame bagel, freshly baked"
          >
            <div className="sun-disc" />
            <div className="hero-stamp">
              <span>MADE SLOW</span>
              <span>♥</span>
              <span>LOVED FAST</span>
            </div>
            {staticHero ? (
              <div
                className="bagel-static"
                role="img"
                aria-label="A freshly baked sesame bagel"
              />
            ) : (
              <Suspense
                fallback={
                  <div
                    className="bagel-static"
                    role="img"
                    aria-label="A freshly baked sesame bagel"
                  />
                }
              >
                <BagelScene />
              </Suspense>
            )}
            <div className="hero-caption">
              <span>01 / 04</span>
              <span>The sesame, in its element</span>
              <ArrowDown size={15} />
            </div>
          </div>
          <div className="hero-bottom">
            <span>GOOD MORNING, BROOKLYN</span>
            <span>40° 41' 59.088" N</span>
            <a href="#story">
              Scroll a little <ArrowDown size={13} />
            </a>
          </div>
        </section>

        <section className="ticker" aria-label="Aube promise">
          <div className="ticker-track">
            {Array.from({ length: 4 }, (_, index) => (
              <span key={index}>
                SLOW FERMENTED <i>✳</i> HAND ROLLED <i>✳</i> ALWAYS WARM{" "}
                <i>✳</i> GOOD MOOD FOOD <i>✳</i>
              </span>
            ))}
          </div>
        </section>

        <section className="manifesto section-pad" id="story">
          <div className="manifesto-aside reveal">
            <span className="eyebrow">A note from the kitchen</span>
            <span className="vertical-rule" />
            <span className="small-note">
              Est. 2018
              <br />
              Brooklyn, New York
            </span>
          </div>
          <div className="manifesto-copy reveal">
            <h2>
              We believe the
              <br />
              morning deserves
              <br />
              <em>to be savored.</em>
            </h2>
            <p>
              Somewhere between the first crackle from the oven and your first
              sip of coffee, the day gets a little softer. We make food for that
              in-between. With good flour, patient hands, and no shortcuts worth
              taking.
            </p>
            <a className="text-link" href="#craft">
              A little about how we do it <ArrowUpRight size={16} />
            </a>
          </div>
          <div className="manifesto-photo reveal">
            <img
              src="https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=900&q=85"
              alt="Golden pastries cooling on a bakery tray"
              loading="lazy"
            />
            <span>THE FIRST BATCH, 6:12 AM</span>
          </div>
        </section>

        <section className="craft section-pad" id="craft">
          <div className="section-heading reveal">
            <div>
              <span className="eyebrow">The art of a good thing</span>
              <h2>
                Simple things,
                <br />
                <em>done slowly.</em>
              </h2>
            </div>
            <p>
              Four little steps. A whole lot of patience.
              <br />
              That's the Aube way.
            </p>
          </div>
          <div className="craft-grid">
            {craftSteps.map(({ number, title, copy, Icon, image }) => (
              <article className="craft-step reveal" key={number}>
                <div className="step-top">
                  <span>{number}</span>
                  <Icon size={18} strokeWidth={1.4} />
                </div>
                <div
                  className="step-image"
                  style={{
                    backgroundImage: `url(https://images.unsplash.com/${image}?auto=format&fit=crop&w=700&q=75)`,
                  }}
                />
                <h3>{title}</h3>
                <p>{copy}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="menu-section section-pad" id="menu">
          <div className="section-heading menu-heading reveal">
            <div>
              <span className="eyebrow">A very good place to start</span>
              <h2>
                Meet your <em>morning.</em>
              </h2>
            </div>
            <p>
              Small-batch favorites, ready when you are.
              <br />
              Everything is better with a little butter.
            </p>
          </div>
          <div className="menu-controls">
            <div
              className="category-tabs"
              role="tablist"
              aria-label="Menu category"
            >
              {categories.map((category) => (
                <button
                  role="tab"
                  aria-selected={activeCategory === category}
                  className={
                    activeCategory === category
                      ? "category-tab active"
                      : "category-tab"
                  }
                  onClick={() => setActiveCategory(category)}
                  key={category}
                >
                  {category}
                </button>
              ))}
            </div>
            <div className="filter-actions">
              <label className="search-field">
                <Search size={15} />
                <input
                  aria-label="Search menu"
                  placeholder="Find a bite"
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                />
              </label>
              <button
                className={
                  vegetarianOnly ? "veg-toggle selected" : "veg-toggle"
                }
                onClick={() => setVegetarianOnly(!vegetarianOnly)}
                aria-pressed={vegetarianOnly}
              >
                <Leaf size={14} /> Veggie
              </button>
              <label className="price-control">
                <span>Up to {formatCurrency(maxPrice)}</span>
                <input
                  aria-label="Maximum menu price"
                  type="range"
                  min="100"
                  max="500"
                  step="10"
                  value={maxPrice}
                  onChange={(event) => setMaxPrice(Number(event.target.value))}
                />
              </label>
            </div>
          </div>
          <div className="menu-grid">
            {filteredItems.map((item, index) => (
              <MenuCard
                key={item.id}
                item={item}
                index={index}
                onAdd={addItem}
              />
            ))}
          </div>
          {filteredItems.length === 0 && (
            <div className="empty-menu">
              Nothing on the menu by that name. Try another little search.
            </div>
          )}
          <div className="menu-footnote">
            <span>All our bagels are hand rolled & kettle boiled.</span>
            <a href="#visit" className="text-link">
              See the whole menu <ArrowRight size={15} />
            </a>
          </div>
        </section>

        <section className="ritual section-pad">
          <div className="ritual-image">
            <img
              src="https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?auto=format&fit=crop&w=1200&q=85"
              alt="Sunlit neighborhood coffee shop"
              loading="lazy"
            />
            <span className="image-label">AUBE, IN THE WILD</span>
          </div>
          <div className="ritual-copy reveal">
            <span className="eyebrow">A little weekday ritual</span>
            <h2>
              Make a morning
              <br />
              of <em>it.</em>
            </h2>
            <p>
              Your first order comes with a little something on us. Pick a
              dozen, find your favorite seat, and let us take it from here.
            </p>
            <div className="offer-code">
              <span>YOUR FIRST MORNING</span>
              <strong>HELLOAUBE</strong>
              <span>TAKE 15% OFF</span>
            </div>
            <a className="button button-dark" href="#menu">
              Start your order <ArrowRight size={16} />
            </a>
          </div>
        </section>

        <section className="visit section-pad" id="visit">
          <div className="visit-copy reveal">
            <span className="eyebrow">A little corner of Brooklyn</span>
            <h2>
              We'll leave
              <br />
              the light <em>on.</em>
            </h2>
            <p>
              Come for the bagel. Stay for the slow morning. Our door's open
              every day, early and easy.
            </p>
            <a
              className="button button-dark"
              href="https://maps.google.com/?q=Brooklyn+NY"
              target="_blank"
              rel="noreferrer"
            >
              Come find us <ArrowUpRight size={16} />
            </a>
          </div>
          <div className="visit-details reveal">
            <div className="visit-pin">
              <MapPin size={19} />
            </div>
            <div>
              <span className="eyebrow">Find the little yellow door</span>
              <p>
                184 Wythe Avenue
                <br />
                Brooklyn, NY 11249
              </p>
            </div>
            <div className="hours">
              <span>EVERY DAY</span>
              <span>7:00 AM — 3:00 PM</span>
            </div>
            <div className="visit-weather">
              Come as you are. We'll put the kettle on.
            </div>
          </div>
        </section>
      </main>

      <footer className="site-footer">
        <div className="footer-main">
          <Link to="/" className="wordmark">
            aube<span>®</span>
          </Link>
          <p>
            Good mornings,
            <br />
            made by hand.
          </p>
          <div className="footer-links">
            <a href="#menu">The menu</a>
            <a href="#story">Our story</a>
            <a href="#visit">Say hello</a>
            <Link to="/admin">Order desk</Link>
            <a href="mailto:bonjour@aube.eats">Email us</a>
          </div>
          <div className="footer-social">
            <a href="https://instagram.com" aria-label="Instagram">
              IG <ArrowUpRight size={13} />
            </a>
            <a href="https://tiktok.com" aria-label="TikTok">
              TK <ArrowUpRight size={13} />
            </a>
          </div>
        </div>
        <div className="footer-bottom">
          <span>© AUBE EATS 2025</span>
          <span>MADE SLOW IN BROOKLYN, NY</span>
          <a
            href="#top"
            onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}
          >
            Back to the top ↑
          </a>
        </div>
      </footer>

      {cartOpen && (
        <div
          className="overlay"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) setCartOpen(false);
          }}
        >
          <aside className="cart-drawer" aria-label="Shopping bag">
            <div className="drawer-heading">
              <div>
                <span className="eyebrow">Your little bag</span>
                <h2>Good choice.</h2>
              </div>
              <button
                className="icon-button"
                onClick={() => setCartOpen(false)}
                aria-label="Close bag"
              >
                <X size={19} />
              </button>
            </div>
            {items.length ? (
              <>
                <div className="cart-items">
                  {items.map(({ item, quantity }) => (
                    <div className="cart-item" key={item.id}>
                      <img src={item.image} alt="" />
                      <div className="cart-item-copy">
                        <strong>{item.name}</strong>
                        <span>{formatCurrency(item.price)}</span>
                        <div className="quantity-control">
                          <button
                            onClick={() => update(item.id, quantity - 1)}
                            aria-label={`Remove one ${item.name}`}
                          >
                            <Minus size={13} />
                          </button>
                          <span>{quantity}</span>
                          <button
                            onClick={() => update(item.id, quantity + 1)}
                            aria-label={`Add one ${item.name}`}
                          >
                            <Plus size={13} />
                          </button>
                        </div>
                      </div>
                      <b>{formatCurrency(item.price * quantity)}</b>
                    </div>
                  ))}
                </div>
                <div className="cart-summary">
                  <div>
                    <span>Subtotal</span>
                    <strong>{formatCurrency(subtotal)}</strong>
                  </div>
                  <p>Delivery & taxes calculated at checkout.</p>
                  <button
                    className="button button-dark checkout-button"
                    onClick={() => {
                      setCheckoutOpen(true);
                      setCartOpen(false);
                    }}
                  >
                    Continue to checkout <ArrowRight size={16} />
                  </button>
                  <button
                    className="keep-shopping"
                    onClick={() => setCartOpen(false)}
                  >
                    Keep looking around
                  </button>
                </div>
              </>
            ) : (
              <div className="empty-cart">
                <ShoppingBag size={28} strokeWidth={1.2} />
                <p>Your bag's on a little break.</p>
                <button
                  className="text-link"
                  onClick={() => {
                    setCartOpen(false);
                    document
                      .querySelector("#menu")
                      ?.scrollIntoView({ behavior: "smooth" });
                  }}
                >
                  Find something lovely <ArrowRight size={15} />
                </button>
              </div>
            )}
          </aside>
        </div>
      )}

      {checkoutOpen && (
        <div
          className="overlay"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) setCheckoutOpen(false);
          }}
        >
          <div className="checkout-modal">
            <div className="drawer-heading">
              <div>
                <span className="eyebrow">The final little step</span>
                <h2>Where to, friend?</h2>
              </div>
              <button
                className="icon-button"
                onClick={() => setCheckoutOpen(false)}
                aria-label="Close checkout"
              >
                <X size={19} />
              </button>
            </div>
            <form onSubmit={placeOrder} className="checkout-form">
              <label>
                Your name
                <input
                  required
                  name="name"
                  placeholder="First and last name"
                  autoComplete="name"
                />
              </label>
              <label>
                Email address
                <input
                  required
                  name="email"
                  type="email"
                  placeholder="you@example.com"
                  autoComplete="email"
                />
              </label>
              <label>
                Delivery address
                <input
                  required
                  name="address"
                  placeholder="Street and apartment"
                  autoComplete="street-address"
                />
              </label>
              <fieldset>
                <div className="coupon-row">
                  <label htmlFor="coupon-code">
                    Little welcome code
                    <input
                      id="coupon-code"
                      value={couponCode}
                      onChange={(event) => setCouponCode(event.target.value)}
                      placeholder="HELLOAUBE"
                    />
                  </label>
                  <button
                    className="coupon-apply"
                    type="button"
                    onClick={applyCoupon}
                  >
                    Apply
                  </button>
                </div>
                {couponMessage && (
                  <p
                    className={
                      appliedCoupon
                        ? "coupon-message applied"
                        : "coupon-message"
                    }
                    role="status"
                  >
                    {couponMessage}
                  </p>
                )}
                <legend>Payment</legend>
                <div className="payment-option selected">
                  <span>Pay on arrival</span>
                  <span className="payment-hint">Cash</span>
                </div>
              </fieldset>
              <div className="checkout-total">
                <span>Total, including delivery</span>
                <strong>
                  {formatCurrency(
                    subtotal *
                      (1 - (appliedCoupon?.discountPercent ?? 0) / 100) +
                      3.5,
                  )}
                </strong>
              </div>
              <button
                className="button button-dark checkout-button"
                type="submit"
              >
                Place my order <ArrowRight size={16} />
              </button>
              <p className="demo-note">
                {authError && (
                  <span className="auth-error" role="alert">
                    {authError}
                    <br />
                  </span>
                )}
                {accessToken
                  ? "Cash on delivery. Online payment is not available yet."
                  : "Guest preview only. Sign in to place a saved order."}
              </p>
            </form>
          </div>
        </div>
      )}

      {orderPlaced && (
        <div className="overlay">
          <div className="order-success">
            <span className="success-mark">
              <Check size={26} />
            </span>
            <span className="eyebrow">
              {demoOrder ? "Guest preview order" : "Order confirmed"}
            </span>
            <h2>
              Your morning
              <br />
              <em>is in good hands.</em>
            </h2>
            <p>
              {demoOrder ? (
                <>
                  <strong>{orderId}</strong> is a preview only. Sign in to place
                  an order with the kitchen.
                </>
              ) : (
                <>
                  Order <strong>{orderId}</strong> is on its way to the kitchen.
                  We'll have it at your door in about 25 minutes.
                </>
              )}
            </p>
            <div className="order-progress">
              <span className="progress-current">Order placed</span>
              {!demoOrder && (
                <Link
                  className="text-link track-order-link"
                  to={`/orders/${orderId}`}
                  onClick={() => setOrderPlaced(false)}
                >
                  Track this order <ArrowRight size={15} />
                </Link>
              )}
              <span>In the oven</span>
              <span>On its way</span>
            </div>
            <button
              className="button button-dark checkout-button"
              onClick={() => {
                setOrderPlaced(false);
                window.scrollTo({ top: 0, behavior: "smooth" });
              }}
            >
              Lovely, thank you <ArrowRight size={16} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function MenuCard({
  item,
  index,
  onAdd,
}: {
  item: MenuItem;
  index: number;
  onAdd: (item: MenuItem) => void;
}) {
  return (
    <article
      className="menu-card reveal"
      style={{ animationDelay: `${index * 60}ms` }}
    >
      <div className="menu-card-image">
        <img src={item.image} alt={item.name} loading="lazy" />
        {item.badge && <span className="menu-badge">{item.badge}</span>}
        <button
          className="quick-add"
          onClick={() => onAdd(item)}
          aria-label={`Add ${item.name} to your bag`}
        >
          <Plus size={17} />
        </button>
      </div>
      <div className="menu-card-meta">
        <div>
          <h3>{item.name}</h3>
          <p>{item.description}</p>
        </div>
        <strong>{formatCurrency(item.price)}</strong>
      </div>
      <div className="menu-card-tags">
        {item.vegetarian && (
          <span>
            <Leaf size={11} /> VEGETARIAN
          </span>
        )}
        <span>{item.category.toUpperCase()}</span>
      </div>
    </article>
  );
}

function OrderStatus() {
  const { orderId = "" } = useParams();
  const navigate = useNavigate();
  const token = useAuth((state) => state.accessToken);
  const [status, setStatus] = useState("PLACED");
  const [connected, setConnected] = useState(false);
  useEffect(() => {
    if (!token || !orderId) return;
    const realtimeUrl =
      import.meta.env.VITE_REALTIME_URL ??
      (window.location.port === "5173"
        ? "http://localhost:4000"
        : window.location.origin);
    const socket = io(realtimeUrl, {
      auth: { token },
      transports: ["websocket"],
    });
    socket.on("connect", () => {
      setConnected(true);
      socket.emit("order:watch", orderId, (result: { status?: string }) => {
        if (result.status) setStatus(result.status);
      });
    });
    socket.on("disconnect", () => setConnected(false));
    socket.on("order:status", (event: { status: string }) =>
      setStatus(event.status),
    );
    return () => {
      socket.disconnect();
    };
  }, [token, orderId]);
  const statusSteps = ["PLACED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED"];
  const statusLabels = [
    "Order placed",
    "In the oven",
    "On its way",
    "Delivered",
  ];
  const activeStep = statusSteps.indexOf(status);
  return (
    <main className="order-page">
      <button className="text-link" onClick={() => navigate("/")}>
        <ArrowLeft size={15} /> Back to Aube
      </button>
      <div className="order-success">
        <span className="success-mark">
          <Check size={26} />
        </span>
        <span className="eyebrow">
          {connected ? "Live order status" : "Order status"}
        </span>
        <h1>
          Something good
          <br />
          <em>
            {status === "DELIVERED" ? "is at your door." : "is on its way."}
          </em>
        </h1>
        <p>
          Order <strong>{orderId}</strong> is{" "}
          {status.replaceAll("_", " ").toLowerCase()}.{" "}
          {connected
            ? "Updates are coming in live."
            : token
              ? "Reconnecting to live updates…"
              : "Sign in to connect live order updates."}
        </p>
        <div className="order-progress">
          {statusLabels.map((label, index) => (
            <span
              className={index <= activeStep ? "progress-current" : ""}
              key={label}
            >
              {label}
            </span>
          ))}
        </div>
        <Link className="button button-dark" to="/">
          Back to the good stuff <ArrowRight size={16} />
        </Link>
      </div>
    </main>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Storefront />} />
        <Route path="/orders/:orderId" element={<OrderStatus />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="*" element={<Storefront />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
type AdminOrder = {
  id: string;
  userId: string;
  status: string;
  total: number;
  deliveryAddress: string;
  createdAt: string;
};

type AdminStats = {
  orders: number;
  registeredUsers: number;
  activeMenuItems: number;
  revenue: number;
  revenueByDay: Record<string, number>;
  statusBreakdown: Record<string, number>;
};

function AdminDashboard() {
  const token = useAuth((state) => state.accessToken);
  const [stats, setStats] = useState<AdminStats | null>(null);
  const [orders, setOrders] = useState<AdminOrder[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!token) return;
    let cancelled = false;
    Promise.all([
      api.get("/admin/dashboard"),
      api.get("/admin/orders", { params: { page: 0, size: 20 } }),
    ])
      .then(([dashboard, orderPage]) => {
        if (cancelled) return;
        setStats(dashboard.data);
        setOrders(orderPage.data.content);
        setError("");
      })
      .catch(() => {
        if (!cancelled)
          setError(
            "The order desk could not load. Check administrator access and API availability.",
          );
      });
    return () => {
      cancelled = true;
    };
  }, [token]);

  async function updateOrder(orderId: string, status: string) {
    try {
      const { data } = await api.patch(
        `/admin/orders/${orderId}/status`,
        null,
        { params: { status } },
      );
      setOrders((current) =>
        current.map((order) =>
          order.id === orderId ? { ...order, status: data.status } : order,
        ),
      );
      setStats((current) =>
        current
          ? {
              ...current,
              statusBreakdown: {
                ...current.statusBreakdown,
                [status]: (current.statusBreakdown[status] ?? 0) + 1,
              },
            }
          : current,
      );
    } catch {
      setError(
        "That order status could not be changed. Refresh and try again.",
      );
    }
  }

  const currency = new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  });
  const revenueDays = Object.entries(stats?.revenueByDay ?? {});
  const highestRevenue = Math.max(
    1,
    ...revenueDays.map(([, amount]) => amount),
  );
  const loading = Boolean(token) && !stats && !error;
  const visibleError = token
    ? error
    : "Sign in with an administrator account to open the order desk.";
  const nextStatus: Record<string, string> = {
    PLACED: "PREPARING",
    PREPARING: "OUT_FOR_DELIVERY",
    OUT_FOR_DELIVERY: "DELIVERED",
  };

  return (
    <main className="admin-shell">
      <header className="admin-header">
        <Link to="/" className="wordmark">
          aube<span>®</span>
        </Link>
        <span className="admin-label">
          <ClipboardList size={15} /> ORDER DESK
        </span>
        <Link to="/" className="text-link">
          Back to storefront <ArrowRight size={14} />
        </Link>
      </header>
      <section className="admin-content">
        <div className="admin-title">
          <div>
            <span className="eyebrow">Brooklyn · daily operations</span>
            <h1>Morning desk</h1>
          </div>
          <span className="admin-date">{adminToday}</span>
        </div>
        {visibleError && (
          <p className="admin-error" role="alert">
            {visibleError}
          </p>
        )}
        {loading ? (
          <p className="admin-loading">Pulling the morning numbers…</p>
        ) : (
          stats && (
            <>
              <div className="admin-metrics">
                <article>
                  <span>GROSS REVENUE</span>
                  <strong>{currency.format(stats.revenue)}</strong>
                  <small>Delivered orders</small>
                </article>
                <article>
                  <span>ALL ORDERS</span>
                  <strong>{stats.orders}</strong>
                  <small>Across every status</small>
                </article>
                <article>
                  <span>REGISTERED PEOPLE</span>
                  <strong>{stats.registeredUsers}</strong>
                  <small>Customer accounts</small>
                </article>
                <article>
                  <span>MENU ITEMS</span>
                  <strong>{stats.activeMenuItems}</strong>
                  <small>Currently available</small>
                </article>
              </div>
              <section className="admin-chart">
                <div className="admin-section-title">
                  <div>
                    <span className="eyebrow">
                      <BarChart3 size={14} /> Last seven days
                    </span>
                    <h2>Delivered revenue</h2>
                  </div>
                  <strong>
                    {currency.format(
                      Math.max(...revenueDays.map(([, amount]) => amount), 0),
                    )}
                    <small> peak day</small>
                  </strong>
                </div>
                <div className="revenue-bars">
                  {revenueDays.map(([day, amount]) => (
                    <div className="revenue-day" key={day}>
                      <strong>{currency.format(amount)}</strong>
                      <div className="bar-track">
                        <span
                          style={{
                            height: `${Math.max(3, (amount / highestRevenue) * 100)}%`,
                          }}
                        />
                      </div>
                      <small>
                        {new Intl.DateTimeFormat("en-US", {
                          weekday: "short",
                          timeZone: "UTC",
                        }).format(new Date(`${day}T12:00:00Z`))}
                      </small>
                    </div>
                  ))}
                </div>
              </section>
              <section className="admin-orders">
                <div className="admin-section-title">
                  <div>
                    <span className="eyebrow">Kitchen and delivery</span>
                    <h2>Recent orders</h2>
                  </div>
                  <span>{orders.length} shown</span>
                </div>
                <div className="admin-table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>ORDER</th>
                        <th>PLACED</th>
                        <th>DELIVERY</th>
                        <th>TOTAL</th>
                        <th>STATUS</th>
                      </tr>
                    </thead>
                    <tbody>
                      {orders.map((order) => (
                        <tr key={order.id}>
                          <td>
                            <strong>
                              {order.id.slice(0, 8).toUpperCase()}
                            </strong>
                          </td>
                          <td>
                            {new Date(order.createdAt).toLocaleTimeString(
                              "en-US",
                              { hour: "numeric", minute: "2-digit" },
                            )}
                          </td>
                          <td>{order.deliveryAddress}</td>
                          <td>{currency.format(order.total)}</td>
                          <td>
                            {nextStatus[order.status] ? (
                              <select
                                aria-label={`Update order ${order.id}`}
                                value={order.status}
                                onChange={(event) =>
                                  updateOrder(order.id, event.target.value)
                                }
                              >
                                <option value={order.status}>
                                  {order.status.replaceAll("_", " ")}
                                </option>
                                <option value={nextStatus[order.status]}>
                                  {nextStatus[order.status].replaceAll(
                                    "_",
                                    " ",
                                  )}
                                </option>
                              </select>
                            ) : (
                              <span className="status-final">
                                {order.status.replaceAll("_", " ")}
                              </span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  {orders.length === 0 && (
                    <p className="admin-empty">
                      No orders yet. The first fresh batch is waiting.
                    </p>
                  )}
                </div>
              </section>
            </>
          )
        )}
      </section>
    </main>
  );
}
