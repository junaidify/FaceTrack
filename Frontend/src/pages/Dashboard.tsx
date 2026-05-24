import { useRef, useState, useEffect } from "react";
import Webcam from "react-webcam";
import axios from "axios";

interface SessionOption {
  id: string;
  classId: string;
  status: string;
  startedAt: string;
}

const Dashboard = () => {
  const webRef = useRef<Webcam | null>(null);
  const [webcamImage, setWebcamImage] = useState<string>("");
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [sessionId, setSessionId] = useState<string>("");
  const [activeSessions, setActiveSessions] = useState<SessionOption[]>([]);
  const [result, setResult] = useState<string>("");

  // Build auth header from stored token
  const authHeader = () => {
    const token = localStorage.getItem("access_token");
    return token ? { Authorization: `Bearer ${token}` } : {};
  };

  // Fetch active sessions on mount
  useEffect(() => {
    const fetchSessions = async () => {
      try {
        const res = await axios.get("/api/v1/sessions", {
          headers: authHeader(),
        });
        const active = res.data.filter(
          (s: SessionOption) => s.status === "ACTIVE"
        );
        setActiveSessions(active);
        if (active.length === 1) {
          setSessionId(active[0].id);
        }
      } catch (err) {
        console.error("Failed to fetch sessions", err);
      }
    };
    fetchSessions();
  }, []);

  const getScreenShot = () => {
    if (webRef.current !== null) {
      const imageSrc = webRef.current.getScreenshot();
      if (imageSrc) {
        setWebcamImage(imageSrc);
        setResult("");
      } else {
        console.error("Failed to capture screen");
      }
    }
  };

  const sendImage = async () => {
    if (!sessionId) {
      setResult("Please select an active session first.");
      return;
    }
    if (!webcamImage) {
      setResult("Capture an image first.");
      return;
    }

    setIsLoading(true);
    setResult("");
    try {
      const res = await axios.post(
        "/api/v1/attendance/mark",
        { sessionId, image: webcamImage },
        { headers: authHeader() }
      );
      const data = res.data;
      if (data.matched) {
        const msg = data.alreadyMarked
          ? `${data.studentName} (${data.rollNo}) — already marked present.`
          : `${data.studentName} (${data.rollNo}) — marked PRESENT (similarity: ${(data.similarity * 100).toFixed(1)}%)`;
        setResult(msg);
      } else {
        setResult(
          `No match found${data.similarity ? ` (best similarity: ${(data.similarity * 100).toFixed(1)}%)` : ""}.`
        );
      }
    } catch (err: any) {
      const msg =
        err.response?.data?.message || err.response?.data?.error || "Request failed";
      setResult(`Error: ${msg}`);
      console.error("Couldn't send the image!", err);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ padding: "1rem" }}>
      {/* Session selector */}
      <div style={{ marginBottom: "1rem", textAlign: "center" }}>
        <label htmlFor="session-select" style={{ marginRight: 8 }}>
          Active Session:
        </label>
        <select
          id="session-select"
          value={sessionId}
          onChange={(e) => setSessionId(e.target.value)}
        >
          <option value="">-- Select a session --</option>
          {activeSessions.map((s) => (
            <option key={s.id} value={s.id}>
              {s.id.slice(0, 8)}… (started {new Date(s.startedAt).toLocaleTimeString()})
            </option>
          ))}
        </select>
      </div>

      {/* Webcam + buttons */}
      <div
        style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          gap: "1rem",
        }}
      >
        <Webcam
          ref={webRef}
          screenshotFormat="image/jpeg"
          style={{ textAlign: "center" }}
          mirrored={true}
          width={"300px"}
          height={"200px"}
        />
        <div style={{ display: "flex", flexDirection: "column", gap: "0.5rem" }}>
          <button onClick={getScreenShot}>Capture</button>
          <button onClick={sendImage} disabled={isLoading || !sessionId}>
            {isLoading ? "Sending…" : "Mark Attendance"}
          </button>
        </div>
      </div>

      {/* Preview + result */}
      <div style={{ textAlign: "center", marginTop: "1rem" }}>
        {webcamImage && (
          <img
            src={webcamImage}
            alt="Captured"
            style={{ maxWidth: 300, border: "1px solid #ccc" }}
          />
        )}
        {result && (
          <p style={{ marginTop: "0.5rem", fontWeight: "bold" }}>{result}</p>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
